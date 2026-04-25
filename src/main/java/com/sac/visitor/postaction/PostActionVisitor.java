package com.sac.visitor.postaction;

import com.sac.model.message.ActionContext;
import com.sac.strategy.action.*;
import org.springframework.web.socket.WebSocketSession;

public interface PostActionVisitor {

    void visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext);
    void visit(Kamikaze kamikaze, WebSocketSession webSocketSession, ActionContext actionContext);
    void visit(Evolve evolve, WebSocketSession webSocketSession, ActionContext actionContext);
    void visit(AttackAndCapture attackAndCapture, WebSocketSession webSocketSession, ActionContext actionContext);
    void visit(BlackOut blackOut, WebSocketSession webSocketSession, ActionContext actionContext);
}

