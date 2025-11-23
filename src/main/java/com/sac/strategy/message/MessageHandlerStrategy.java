package com.sac.strategy.message;

import com.sac.model.message.DefaultMessage;
import com.sac.model.message.DefaultMessage.Type;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public interface MessageHandlerStrategy {

    void handle(WebSocketSession webSocketSession, DefaultMessage message, String roomId) throws IOException;
    Type getStrategy();
}
