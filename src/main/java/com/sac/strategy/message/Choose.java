package com.sac.strategy.message;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.message.DefaultMessage;
import com.sac.model.message.DefaultMessage.Type;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class Choose implements MessageHandlerStrategy {

    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final Map<String, Map<String, Integer>> roomsRespondedPlayers = new ConcurrentHashMap<>();

    @Override
    public Type getStrategy() {
        return Type.CHOOSE;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, DefaultMessage message, String roomId) throws IOException {
        GameState gameState = gameStateService.getGameState(roomId);
        if (!preProcessChecks(webSocketSession, message.getContent(), gameState))
            return;
        if (!roomsRespondedPlayers.containsKey(roomId))
            roomsRespondedPlayers.put(roomId, new HashMap<>());
        String respondedPlayerId = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        Map<String, Integer> respondedPlayers = roomsRespondedPlayers.get(roomId);
        if (respondedPlayers.containsKey(respondedPlayerId)) {
            messageService.sendToSender(
                    webSocketSession,
                    "Your response is already recorded, wait for opponent",
                    ServerResponse.Type.ERROR);
        } else {
            respondedPlayers.put(respondedPlayerId, Integer.parseInt(message.getContent()));
            messageService.sendToSender(webSocketSession, "Your response is recorded as "+message.getContent());
            int totalPlayersInTheRoom = gameState.getPlayerCount();
            if (totalPlayersInTheRoom == respondedPlayers.size()) {
                processResponses(roomId, gameState, respondedPlayers);
            }
        }
    }

    private boolean preProcessChecks(WebSocketSession webSocketSession, String chosenNumber, GameState gameState) {
        if (gameState.isActionPending()) {
            String errorMsg = String.format("%s needs to perform action before choosing",
                            gameState.getCurrentPlayerId());
            messageService.sendToSender(webSocketSession,
                    MessageFormat.systemError(errorMsg),
                    ServerResponse.Type.ERROR);
            return false;
        }
        if (!Pattern.matches("^[0-5]$", chosenNumber)) {
            String errorMsg = "Only positions from 0 to 5 are allowed";
            messageService.sendToSender(webSocketSession,
                    MessageFormat.systemError(errorMsg),
                    ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }

    private void processResponses(String roomId, GameState gameState, Map<String, Integer> respondedPlayers) throws IOException {
        String  chosenPlayerId = gameState.getCurrentPlayerId();
        int chosenPosition = respondedPlayers.get(chosenPlayerId);
        String guessedPlayerId = gameStateService.getOpponentId(roomId, chosenPlayerId);
        int guessedPosition = respondedPlayers.get(guessedPlayerId);
        if (chosenPosition != guessedPosition) {
            gameStateService.setCurrentPlayerId(roomId, chosenPlayerId);
            gameStateService.setActionPendingOn(roomId, chosenPosition);
            messageService.broadcastMessage(
                    String.format("%s won, time for action on position %s", chosenPlayerId, chosenPosition),
                    roomId, ServerResponse.Type.INFO);
            tryValidActionPosition(roomId, chosenPlayerId, chosenPosition, guessedPlayerId);
        } else {
            gameStateService.setCurrentPlayerId(roomId, guessedPlayerId);
            messageService.broadcastMessage(
                    String.format("%s won, gear up to choose", guessedPlayerId),
                    roomId, ServerResponse.Type.INFO);
        }
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        respondedPlayers.clear();
    }

    public void tryValidActionPosition(String roomId, String username,
                                                int chosenPosition, String opponentName) {
        GameState gameState = gameStateService.getGameState(roomId);
        Position position = gameStateService.getPlayerPosition(roomId, username, chosenPosition);
        Actor actor = position.getActor();
        if (actor != null && actor.isFrozen()) {
            messageService.broadcastMessage(
                    MessageFormat.frozenTrouble(chosenPosition, actor.getCurrentState()), roomId);
            changeChooseOwnership(roomId, opponentName, gameState);
        } else if (position.isCapturedByOpponent()) {
            messageService.broadcastMessage(
                    MessageFormat.capturedTrouble(position.getBelongsTo(), position.getPositionId()), roomId);
            changeChooseOwnership(roomId, opponentName, gameState);
        }
    }

    private void changeChooseOwnership(String roomId, String opponentName, GameState gameState) {
        gameState.setActionPending(false);
        gameState.setActionPendingOn(-1);
        gameState.setCurrentPlayerId(opponentName);
        messageService.broadcastMessage(MessageFormat.chooseMessage(opponentName), roomId);
    }
}
