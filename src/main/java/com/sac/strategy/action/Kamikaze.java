package com.sac.strategy.action;

import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.service.PointsService;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.sac.strategy.action.GameAction.KAMIKAZE;

@Component
@RequiredArgsConstructor
public class Kamikaze implements Action {

    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final PointsService pointsService;

    @Override
    public GameAction getActionType() {
        return KAMIKAZE;
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {

        if (preProcessAction(webSocketSession, actionContext, roomId)) {

            GameState gameState = gameStateService.getGameState(roomId);
            String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
            Player opponent = gameState.getOpponent(username);

            Integer destinationPositionToPerformAction = actionContext.getDestinationPosition();
            Integer sourcePositionToPerformAction = actionContext.getSourcePosition();
            Integer actionPendingPosition = gameState.getActionPendingOn();

            Position playerPosition = gameState.getPlayerPosition(username, actionPendingPosition);
            Position opponentPosition = opponent.getPositions()[destinationPositionToPerformAction];

            if (sourcePositionToPerformAction != null) {
                playerPosition.restorePosition();
                messageService.broadcastMessage(MessageFormat.kamikazeSuccessAction(username, sourcePositionToPerformAction), roomId);
            } else {
                opponentPosition.restorePosition();
                messageService.broadcastMessage(MessageFormat.kamikazeSuccessAction(opponent.getUsername(), destinationPositionToPerformAction), roomId);
            }
            postProcessAction(gameState, username, roomId);
        }
    }

    private boolean preProcessAction(WebSocketSession webSocketSession, ActionContext context, String roomId) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        Integer opponentPositionId = context.getDestinationPosition();

        if (!gameState.isActionPending() ||
            !gameState.getCurrentPlayerId().equals(username) ||
            gameState.getActionPendingOn() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (opponentPositionId == null && context.getSourcePosition() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noDestinationProvided());
            return false;
        }

        Position position = gameState.getPlayerPosition(username, gameState.getActionPendingOn());
        Actor actor = position.getActor();

        if (actor == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noActorPresent(gameState.getActionPendingOn()));
            return false;
        } else if (!actor.getAllowedActions().contains(getActionType())) {
            messageService.sendToSender(webSocketSession, MessageFormat.actorCannotPerform(
                    actor.getCurrentState(), getActionType()));
            return false;
        }
        return true;
    }

    private void postProcessAction(GameState gameState, String username, String roomId) {
        pointsService.addPoints(roomId, username, pointsForSuccessfulAction());
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 1;
    }
}
