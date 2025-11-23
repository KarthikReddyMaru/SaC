package com.sac.factory;

import com.sac.model.message.MessageEnvelope;
import com.sac.model.message.MessageEnvelope.Type;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface EnvelopeHandler {
    Type getType();
    void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws IOException;
}
