package com.sac.strategy.action;

import com.sac.factory.ActorFactory;
import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.util.SocketSessionUtil;
import com.sac.visitor.postaction.PostActionVisitor;
import com.sac.visitor.preaction.PreActionVisitor;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.sac.strategy.action.GameAction.PROMOTE;

@Slf4j
@Component
@RequiredArgsConstructor
public class Promote implements Action {

    private final GameStateService gameStateService;

    @Override
    public GameAction getActionType() {
        return PROMOTE;
    }

    @Override
    public boolean preAction(PreActionVisitor preActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext) {
        return preActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override @WithSpan("actio.promote")
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {
        String playerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        Specialization requestedTransition = actionContext.getSpecialization();
        GameState gameState = gameStateService.getGameState(roomId);
        Position position = gameState.getPlayerPosition(playerId, gameState.getActionPendingOn());
        position.setActor(ActorFactory.getInstance(requestedTransition));

        log.info("PROMOTED to {}", position.getActor().getCurrentState().name());
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
