package com.sac.visitor.preaction;

import com.sac.model.message.ActionContext;
import com.sac.strategy.action.AttackAndCapture;
import com.sac.strategy.action.Evolve;
import com.sac.strategy.action.Kamikaze;
import com.sac.strategy.action.Spawn;
import org.springframework.web.socket.WebSocketSession;

public interface PreActionVisitor {

    boolean visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext);
    boolean visit(Kamikaze kamikaze, WebSocketSession webSocketSession, ActionContext actionContext);
    boolean visit(Evolve evolve, WebSocketSession webSocketSession, ActionContext actionContext);
    boolean visit(AttackAndCapture attackAndCapture, WebSocketSession webSocketSession, ActionContext actionContext);

}
