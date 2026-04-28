package com.sac.strategy.action;

import com.sac.model.Position;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class Capture implements Action {

    private final GameStateService gameStateService;

    @Override
    public GameAction getActionType() {
        return GameAction.CAPTURE;
    }

    @Override
    public boolean preAction(PreActionVisitor preActionVisitor, WebSocketSession webSocketSession,
                             ActionContext actionContext) {
        return preActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override @WithSpan("action.capture")
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {

        String playerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        Integer opponentPositionId = actionContext.getDestinationPosition();
        String opponentId = actionContext.getDestinationPositionHolder();
        Position opponentPosition = gameStateService.getGameState(roomId)
                                                    .getPlayerPosition(opponentId, opponentPositionId);
        opponentPosition.capturePosition(playerId);
        log.info("Captured {} position: {}", gameStateService.getUsernameFromId(actionContext.getDestinationPositionHolder(), roomId),
                 actionContext.getDestinationPosition());
    }

    @Override
    public void postAction(PostActionVisitor postActionVisitor, WebSocketSession webSocketSession,
                           ActionContext actionContext) {
        postActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 3;
    }
}
