package com.sac.strategy.action;

import com.sac.model.message.ActionContext;
import com.sac.visitor.postaction.PostActionVisitor;
import com.sac.visitor.preaction.PreActionVisitor;
import org.springframework.web.socket.WebSocketSession;

public interface Action {

    boolean preAction(PreActionVisitor preActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext);
    void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId);
    void postAction(PostActionVisitor postActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext);
    int pointsForSuccessfulAction();
    GameAction getActionType();

}