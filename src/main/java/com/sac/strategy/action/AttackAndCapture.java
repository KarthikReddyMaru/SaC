package com.sac.strategy.action;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
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

    @Override
    public GameAction getActionType() {
        return GameAction.ATTACK_AND_CAPTURE;
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {
        GameState gameState = gameStateService.getGameState(roomId);

        String playerUserName = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        int playerPositionId = gameState.getActionPendingOn();
        if (playerPositionId == -1) { messageService.sendToSender(webSocketSession, MessageFormat.illegalAction()); return; };
        Position playerPosition = gameStateService.getPlayerPosition(roomId, playerUserName, playerPositionId);

        String opponentUsername = gameStateService.getOpponentId(roomId, playerUserName);
        Integer opponentPositionId = actionContext.getDestinationPosition();
        if (opponentPositionId == null) { messageService.sendToSender(webSocketSession, MessageFormat.illegalAction()); return; };
        Position opponentPosition = gameStateService.getPlayerPosition(roomId, opponentUsername, opponentPositionId);

        if (preProcessChecks(webSocketSession, playerUserName,
                gameState, playerPosition, opponentPosition)) {
            opponentPosition.capturePosition(playerUserName);
            gameStateService.getPlayer(roomId, playerUserName).addPoints(pointsForSuccessfulAction());
            messageService.sendToSender(roomConnectionService.getUserRegistry().get(playerUserName),
                    MessageFormat.capturePosition(playerUserName, opponentUsername, opponentPositionId));
            gameState.setActionPending(false);
            gameState.setActionPendingOn(-1);
            messageService.broadcastMessage(MessageFormat.chooseMessage(playerUserName), roomId);
        }
    }

    public boolean preProcessChecks(WebSocketSession webSocketSession, String playerName,
                                    GameState gameState, Position playerPosition, Position opponentPosition) {
        if (!gameState.isActionPending() || !gameState.getCurrentPlayerId().equals(playerName)) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        }
        Actor playerActor = playerPosition.getActor();
        if (playerActor == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noActorPresent(playerPosition.getPositionId()));
            return false;
        }
        else if (!playerActor.getAllowedActions().contains(getActionType())) {
            messageService.sendToSender(webSocketSession, MessageFormat.actorCannotPerform(
                    playerActor.getCurrentState(), getActionType()));
            return false;
        }
        else if (opponentPosition.isCapturedByOpponent()) {
            messageService.sendToSender(webSocketSession, "This position is already captured, try another position");
            return false;
        }
        return true;
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 3;
    }
}
