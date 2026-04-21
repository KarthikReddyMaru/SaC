package com.sac.visitor.preaction;

import com.sac.model.message.ActionContext;
import com.sac.strategy.action.Kamikaze;
import org.springframework.web.socket.WebSocketSession;

public interface PreActionVisitor {
    boolean visit(Kamikaze kamikaze, WebSocketSession webSocketSession, ActionContext actionContext);
}
