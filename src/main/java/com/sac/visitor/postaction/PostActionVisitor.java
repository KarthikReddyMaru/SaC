package com.sac.visitor.postaction;

import com.sac.model.message.ActionContext;
import com.sac.strategy.action.*;
import org.springframework.web.socket.WebSocketSession;

public interface PostActionVisitor {

    void visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext);
    void visit(Revert revert, WebSocketSession webSocketSession, ActionContext actionContext);
    void visit(Promote promote, WebSocketSession webSocketSession, ActionContext actionContext);
    void visit(Capture capture, WebSocketSession webSocketSession, ActionContext actionContext);
    void visit(BlackOut blackOut, WebSocketSession webSocketSession, ActionContext actionContext);
}

