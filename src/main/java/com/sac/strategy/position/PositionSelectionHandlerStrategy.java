package com.sac.strategy.position;

import com.sac.model.message.PositionContext;
import com.sac.visitor.prechoose.PreChooseVisitor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface PositionSelectionHandlerStrategy {

    void handle(WebSocketSession webSocketSession, PositionContext message, String roomId) throws IOException;
    PositionSelection getPositionSelectionType();

    default boolean preChoose(PreChooseVisitor preChooseVisitor, WebSocketSession webSocketSession, PositionContext message) {
        return false;
    }
}
