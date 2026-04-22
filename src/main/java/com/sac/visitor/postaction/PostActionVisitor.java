package com.sac.visitor.postaction;

import com.sac.model.message.ActionContext;
import com.sac.strategy.action.Evolve;
import com.sac.strategy.action.Kamikaze;
import org.springframework.web.socket.WebSocketSession;

public interface PostActionVisitor {

    void visit(Kamikaze kamikaze, WebSocketSession webSocketSession, ActionContext actionContext);
    void visit(Evolve evolve, WebSocketSession webSocketSession, ActionContext actionContext);
}

