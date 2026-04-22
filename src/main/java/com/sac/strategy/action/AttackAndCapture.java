package com.sac.strategy.action;

import com.sac.model.Position;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.util.SocketSessionUtil;
import com.sac.visitor.postaction.PostActionVisitor;
import com.sac.visitor.preaction.PreActionVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class AttackAndCapture implements Action {

    private final GameStateService gameStateService;

    @Override
    public GameAction getActionType() {
        return GameAction.ATTACK_AND_CAPTURE;
    }

    @Override
    public boolean preAction(PreActionVisitor preActionVisitor, WebSocketSession webSocketSession,
                             ActionContext actionContext) {
        return preActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {

        String playerUserName = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        Integer opponentPositionId = actionContext.getDestinationPosition();
        String opponentUsername = gameStateService.getOpponentId(roomId, playerUserName);
        Position opponentPosition = gameStateService.getPlayerPosition(roomId, opponentUsername, opponentPositionId);
        opponentPosition.capturePosition(playerUserName);

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
