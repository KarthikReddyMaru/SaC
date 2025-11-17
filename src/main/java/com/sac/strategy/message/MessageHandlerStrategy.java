package com.sac.strategy.message;

import com.sac.model.Message;
import com.sac.model.Message.Type;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public interface MessageHandlerStrategy {

    void handle(WebSocketSession webSocketSession, Message message, String roomId) throws IOException;
    Type getStrategy();
}
