package com.sac.strategy.position;

import com.sac.model.GameState;
import com.sac.model.message.PositionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.ChosenResponseService;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class Choose implements PositionSelectionHandlerStrategy {

    private final Map<String, Map<String, Integer>> roomsRespondedPlayers = new ConcurrentHashMap<>();

    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final ChosenResponseService chosenResponseService;

    @Override
    public PositionSelection getPositionSelectionType() {
        return PositionSelection.CHOOSE;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, PositionContext positionContext, String roomId) throws IOException {
        GameState gameState = gameStateService.getGameState(roomId);
        if (preProcessChecks(webSocketSession, positionContext.getPosition(), gameState)) {
            roomsRespondedPlayers.computeIfAbsent(roomId, (room) -> new HashMap<>());
            String respondedPlayerId = SocketSessionUtil.getUserNameFromSession(webSocketSession);
            Map<String, Integer> respondedPlayers = roomsRespondedPlayers.get(roomId);
            if (respondedPlayers.containsKey(respondedPlayerId)) {
                messageService.sendToSender(
                        webSocketSession,
                        "Your response is already recorded, wait for opponent",
                        ServerResponse.Type.ERROR);
            } else {
                respondedPlayers.put(respondedPlayerId, positionContext.getPosition());
                messageService.sendToSender(webSocketSession, "Your response is recorded as " + positionContext.getPosition());
                messageService.sendMessage(webSocketSession, MessageFormat.chosenResponseMessage(respondedPlayerId), roomId);
                int totalPlayersInTheRoom = gameState.getPlayerCount();
                if (totalPlayersInTheRoom == respondedPlayers.size())
                    chosenResponseService.processChosenResponses(roomId, gameState, respondedPlayers);
            }
        }
    }

    private boolean preProcessChecks(WebSocketSession webSocketSession, int chosenNumber, GameState gameState) {
        if (gameState.isActionPending()) {
            String errorMsg = String.format("%s needs to perform action before choosing",
                            gameState.getCurrentPlayerId());
            messageService.sendToSender(webSocketSession,
                    MessageFormat.systemError(errorMsg),
                    ServerResponse.Type.ERROR);
            return false;
        }
        if (chosenNumber < 1 || chosenNumber > 10) {
            String errorMsg = "Only positions from 1 to 10 are allowed";
            messageService.sendToSender(webSocketSession,
                    MessageFormat.systemError(errorMsg),
                    ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }

}
