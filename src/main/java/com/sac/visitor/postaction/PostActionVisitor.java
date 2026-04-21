package com.sac.visitor.postaction;

import com.sac.strategy.action.Kamikaze;
import org.springframework.web.socket.WebSocketSession;

public interface PostActionVisitor {

    void visit(Kamikaze kamikaze, WebSocketSession webSocketSession);

}

