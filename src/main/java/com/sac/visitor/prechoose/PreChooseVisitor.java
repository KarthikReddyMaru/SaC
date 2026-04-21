package com.sac.visitor.prechoose;

import com.sac.model.message.PositionContext;
import com.sac.strategy.position.Roll;
import org.springframework.web.socket.WebSocketSession;

public interface PreChooseVisitor {

    boolean visit(Roll roll, WebSocketSession webSocketSession, PositionContext positionContext);

}
