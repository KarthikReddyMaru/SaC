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
import com.sac.visitor.postaction.PostActionVisitor;
import com.sac.visitor.preaction.PreActionVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class Spawn implements Action {

    private final GameStateService gameStateService;

    @Override
    public GameAction getActionType() {
        return GameAction.SPAWN;
    }

    @Override
    public boolean preAction(PreActionVisitor preActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext) {
        return preActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        Integer playerPositionId = gameState.getActionPendingOn();
        Position position = gameStateService.getPlayerPosition(roomId, username, playerPositionId);
        Actor actor = ActorFactory.getInstance(Specialization.NOVICE);
        position.setActor(actor);
    }

    @Override
    public void postAction(PostActionVisitor postActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext) {
        postActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 0;
    }
}
