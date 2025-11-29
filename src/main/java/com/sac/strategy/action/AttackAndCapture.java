package com.sac.strategy.action;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.message.ActionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.service.PointsService;
import com.sac.service.RoomConnectionService;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class AttackAndCapture implements Action {

    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final RoomConnectionService roomConnectionService;
    private final PointsService pointsService;

    @Override
    public GameAction getActionType() {
        return GameAction.ATTACK_AND_CAPTURE;
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {

        String playerUserName = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        Integer playerPositionId = gameState.getActionPendingOn();
        Integer opponentPositionId = actionContext.getDestinationPosition();

        if (preProcessAction(webSocketSession, playerUserName, gameState, playerPositionId, opponentPositionId)) {
            String opponentUsername = gameStateService.getOpponentId(roomId, playerUserName);
            Position opponentPosition = gameStateService.getPlayerPosition(roomId, opponentUsername, opponentPositionId);
            opponentPosition.capturePosition(playerUserName);
            postProcessAction(roomId, playerUserName, opponentUsername, opponentPositionId, gameState);
        }
    }

    public boolean preProcessAction(WebSocketSession webSocketSession, String playerName,
                                    GameState gameState, Integer playerPositionId, Integer opponentPositionId) {

        if (!gameState.isActionPending() || !gameState.getCurrentPlayerId().equals(playerName)) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (gameState.getActionPendingOn() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        }

        Position playerPosition = gameState.getPlayerPosition(playerName, playerPositionId);
        Actor playerActor = playerPosition.getActor();
        if (playerActor == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noActorPresent(playerPosition.getPositionId()));
            return false;
        } else if (!playerActor.getAllowedActions().contains(getActionType())) {
            messageService.sendToSender(webSocketSession, MessageFormat.actorCannotPerform(
                    playerActor.getCurrentState(), getActionType()));
            return false;
        } else if (opponentPositionId == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noDestinationProvided());
            return false;
        } else if (gameState.getOpponentPosition(playerName, opponentPositionId).isCapturedByOpponent()) {
            messageService.sendToSender(webSocketSession, MessageFormat.capturedTrouble(playerName, opponentPositionId));
            return false;
        }
        return true;
    }

    private void postProcessAction(String roomId, String playerUserName, String opponentUsername,
                                   Integer opponentPositionId, GameState gameState) {
        messageService.sendToSender(roomConnectionService.getUserRegistry().get(playerUserName),
                MessageFormat.captureSuccessAction(playerUserName, opponentUsername, opponentPositionId));
        pointsService.addPoints(roomId, playerUserName, pointsForSuccessfulAction());
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        messageService.broadcastMessage(MessageFormat.chooseMessage(playerUserName), roomId);
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 3;
    }
}
