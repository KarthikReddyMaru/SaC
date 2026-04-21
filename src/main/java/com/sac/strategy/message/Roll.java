package com.sac.strategy.message;

import com.sac.factory.ActionHandlerRegistry;
import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.message.DefaultMessage;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.GameAction;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.sac.model.message.DefaultMessage.Type;

@Component
@RequiredArgsConstructor
public class Roll implements MessageHandlerStrategy {

    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final ActionHandlerRegistry actionHandlerRegistry;

    @Override
    public void handle(WebSocketSession webSocketSession, DefaultMessage message, String roomId) throws IOException {
        if (preProcessChecks(webSocketSession, message.getContent(), roomId)) {

            GameState gameState = gameStateService.getGameState(roomId);
            String currentPlayer = gameState.getCurrentPlayerId();
            String opponentPlayer = gameState.getOpponent(currentPlayer).getUsername();
            int positionId = Integer.parseInt(message.getContent());
            Position position = gameState.getPlayerPosition(currentPlayer, positionId);

            if (position.isCapturedByOpponent()) {
                messageService.broadcastMessage(MessageFormat.capturedTrouble(opponentPlayer, positionId), roomId);
                gameState.setActionPending(false);
                gameState.setActionPendingOn(null);
                gameState.setCurrentPlayerId(opponentPlayer);
                messageService.broadcastMessage(String.format("%s will roll the dice now", opponentPlayer), roomId);
            } else if (position.getActor() == null) {
                gameState.setActionPending(true);
                gameState.setActionPendingOn(positionId);
                actionHandlerRegistry.getInstance(GameAction.SPAWN).performAction(webSocketSession, null, roomId);
                gameState.setCurrentPlayerId(opponentPlayer);
                messageService.broadcastMessage(String.format("%s will roll the dice now", opponentPlayer), roomId);
            } else {
                String infoMessageForOpponent = String.format("%s is performing action", currentPlayer);
                String actorType = position.getActor().getCurrentState().name();
                messageService.sendToSender(webSocketSession, MessageFormat.performAction(actorType));
                messageService.sendMessage(webSocketSession, infoMessageForOpponent, roomId);
                gameState.setCurrentPlayerId(currentPlayer);
                gameState.setActionPending(true);
                gameState.setActionPendingOn(positionId);
            }
        }
    }

    @Override
    public Type getStrategy() {
        return Type.ROLL;
    }

    public boolean preProcessChecks(WebSocketSession webSocketSession, String rolledNumber, String roomId) {
        GameState gameState = gameStateService.getGameState(roomId);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);

        if (!gameState.getCurrentPlayerId().equals(username)) {
            String message = String.format("Wait for your turn, %s's turn now", gameState.getOpponent(username));
            messageService.sendToSender(webSocketSession, message, ServerResponse.Type.ERROR);
            return false;
        }
        else if (gameState.isActionPending()) {
            String errorMsg = String.format("%s needs to perform action before choosing",
                                            gameState.getCurrentPlayerId());
            messageService.sendToSender(webSocketSession,
                                        MessageFormat.systemError(errorMsg),
                                        ServerResponse.Type.ERROR);
            return false;
        }
        else if (!rolledNumber.matches("[1-9]|10")) {
            String errorMsg = "Only positions from 1 to 10 are allowed";
            messageService.sendToSender(webSocketSession,
                                        MessageFormat.systemError(errorMsg),
                                        ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }
}
