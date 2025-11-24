package com.sac.strategy.message;

import com.sac.model.GameState;
import com.sac.model.message.DefaultMessage;
import com.sac.model.message.DefaultMessage.Type;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.service.RoomConnectionService;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class Choose implements MessageHandlerStrategy {

    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final Map<String, Map<String, Integer>> roomsRespondedPlayers = new ConcurrentHashMap<>();
    private final RoomConnectionService roomConnectionService;

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
            messageService.sendToSender(webSocketSession, "Your response is already recorded, wait for opponent");
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
            messageService.sendToSender(webSocketSession, errorMsg);
            return false;
        }
        if (!Pattern.matches("^[1-6]$", chosenNumber)) {
            String errorMsg = "Only positions from 1 to 6 are allowed";
            messageService.sendToSender(webSocketSession, errorMsg);
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
            messageService.broadcastMessage(String.format("%s won, time for action on position %s",
                    chosenPlayerId, chosenPosition), roomId);
        } else {
            gameStateService.setCurrentPlayerId(roomId, guessedPlayerId);
            messageService.broadcastMessage(String.format("%s won, gear up to choose",
                    guessedPlayerId), roomId);
        }
        respondedPlayers.clear();
    }
}
