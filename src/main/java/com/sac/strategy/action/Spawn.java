package com.sac.strategy.action;

import com.sac.factory.ActorFactory;
import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ActionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class Spawn implements Action {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public GameAction getActionType() {
        return GameAction.SPAWN;
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        if (preProcessChecks(webSocketSession, username, roomId)) {
            GameState gameState = gameStateService.getGameState(roomId);
            Integer playerPositionId = gameState.getActionPendingOn();
            Position position = gameStateService.getPlayerPosition(roomId, username, playerPositionId);
            Actor actor = ActorFactory.getInstance(Specialization.NOVICE);
            position.setActor(actor);
            postProcessAction(roomId, username, gameState);
        }
    }

    private boolean preProcessChecks(WebSocketSession webSocketSession, String username, String roomId) {
        GameState gameState = gameStateService.getGameState(roomId);
        if (gameState.getActionPendingOn() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (!gameState.isActionPending() || !gameState.getCurrentPlayerId().equals(username)) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (gameStateService.getPlayerPosition(roomId, username, gameState.getActionPendingOn()).getActor() != null) {
            String errorMsg = "An actor already present in this position, choose different action";
            messageService.sendToSender(webSocketSession, errorMsg, ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }

    private void postProcessAction(String roomId, String username, GameState gameState) {
        messageService.broadcastMessage(MessageFormat.spawnSuccessAction(username, gameState.getActionPendingOn()), roomId);
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        messageService.broadcastMessage(MessageFormat.chooseMessage(username), roomId);
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 0;
    }
}
