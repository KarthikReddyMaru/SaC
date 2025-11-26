package com.sac.strategy.action;

import com.sac.factory.ActorFactory;
import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ActionContext;
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
        GameState gameState = gameStateService.getGameState(roomId);
        int playerPositionId = gameState.getActionPendingOn();
        if (playerPositionId == -1) { messageService.sendToSender(webSocketSession, MessageFormat.illegalAction()); return; };
        Position position = gameStateService.getPlayerPosition(roomId, username, playerPositionId);
        if (preProcessChecks(webSocketSession, username, gameState, position)) {
            Actor actor = ActorFactory.getInstance(Specialization.NOVICE);
            position.setActor(actor);
            messageService.broadcastMessage(MessageFormat.spawnSuccessAction(username, gameState.getActionPendingOn()), roomId);
            gameState.setActionPending(false);
            gameState.setActionPendingOn(-1);
            messageService.broadcastMessage(MessageFormat.chooseMessage(username), roomId);
        }
    }

    public boolean preProcessChecks(WebSocketSession webSocketSession, String username,
                                     GameState gameState, Position position) {
        if (!gameState.isActionPending() || !gameState.getCurrentPlayerId().equals(username)) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (position.getActor() != null) {
            String errorMsg = "An actor already present in this position, choose different action";
            messageService.sendToSender(webSocketSession, errorMsg);
            return false;
        }
        return true;
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 0;
    }
}
