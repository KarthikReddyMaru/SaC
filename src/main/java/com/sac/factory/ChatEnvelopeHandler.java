package com.sac.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.message.MessageContext;
import com.sac.model.message.MessageEnvelope;
import com.sac.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ChatEnvelopeHandler implements EnvelopeHandler {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @Override
    public MessageEnvelope.Type getType() {
        return MessageEnvelope.Type.CHAT;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws IOException {
        MessageContext messageContext = objectMapper.treeToValue(messageEnvelope.getPayload(), MessageContext.class);
        messageService.sendMessage(webSocketSession, messageContext.getMessage(), roomId);
    }
}
