package com.sac.visitor.preaction;

import com.sac.model.message.ActionContext;
import com.sac.strategy.action.*;
import org.springframework.web.socket.WebSocketSession;

public interface PreActionVisitor {

    boolean visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext);
    boolean visit(Revert revert, WebSocketSession webSocketSession, ActionContext actionContext);
    boolean visit(Promote promote, WebSocketSession webSocketSession, ActionContext actionContext);
    boolean visit(Capture capture, WebSocketSession webSocketSession, ActionContext actionContext);
    boolean visit(BlackOut blackOut, WebSocketSession webSocketSession, ActionContext actionContext);

}
