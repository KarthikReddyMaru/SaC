package com.sac.strategy.action;

import com.sac.model.message.ActionContext;
import com.sac.visitor.postaction.PostActionVisitor;
import com.sac.visitor.preaction.PreActionVisitor;
import org.springframework.web.socket.WebSocketSession;

public interface Action {
    GameAction getActionType();


    void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId);
    int pointsForSuccessfulAction();


    default void preAction(PreActionVisitor preActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext) {}
    default void postAction(PostActionVisitor postActionVisitor, WebSocketSession webSocketSession) {}
}