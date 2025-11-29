package com.sac.strategy.action;

import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
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

    @Override
    public GameAction getActionType() {
        return KAMIKAZE;
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {
        GameState gameState = gameStateService.getGameState(roomId);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        Integer opponentPositionId = actionContext.getDestinationPosition();
        if (preProcessAction(gameState, webSocketSession, username, opponentPositionId)) {

            Integer playerPositionId = gameState.getActionPendingOn();
            Position playerPosition = gameState.getPlayerPosition(username, playerPositionId);
            Actor playerActor = playerPosition.getActor();

            Player opponent = gameState.getOpponent(username);
            Position opponentPosition = opponent.getPositions()[opponentPositionId];
            Actor opponentActor = opponentPosition.getActor();

            if (opponentActor == null || playerActor.getCurrentState().level <= opponentActor.getCurrentState().level) {
                playerPosition.setActor(null);
                messageService.broadcastMessage(
                        MessageFormat.kamikazeSuccessAction(username, opponent.getUsername(), opponentPositionId),
                        roomId);
            } else {
                opponentPosition.setActor(playerPosition.getActor());
                playerPosition.setActor(null);
                messageService.broadcastMessage(MessageFormat.kamikazeSuccessActionWithDegradation(
                        username, opponentPositionId, opponentActor.getCurrentState(), playerActor.getCurrentState()),
                        roomId);
            }
            postProcessAction(gameState, roomId, username);
        }
    }

    private boolean preProcessAction(GameState gameState, WebSocketSession webSocketSession,
                                     String username, Integer opponentPositionId) {
        if (!gameState.isActionPending() || !gameState.getCurrentPlayerId().equals(username)) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (gameState.getActionPendingOn() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (opponentPositionId == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noDestinationProvided());
            return false;
        }

        Position position = gameState.getPlayerPosition(username, gameState.getActionPendingOn());
        Position opponentPosition = gameState.getOpponentPosition(username, opponentPositionId);
        Actor actor = position.getActor();
        if (actor == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noActorPresent(gameState.getActionPendingOn()));
            return false;
        } else if (!actor.getAllowedActions().contains(getActionType())) {
            messageService.sendToSender(webSocketSession, MessageFormat.actorCannotPerform(
                    actor.getCurrentState(), getActionType()));
            return false;
        } else if (opponentPosition.isCapturedByOpponent()) {
            messageService.sendToSender(webSocketSession, MessageFormat.capturedTrouble(
                    opponentPosition.getBelongsTo(), opponentPositionId));
            return false;
        }
        return true;
    }

    private void postProcessAction(GameState gameState, String username, String roomId) {
        gameState.getPlayer(username).addPoints(pointsForSuccessfulAction());
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 1;
    }
}
