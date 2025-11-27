package com.sac.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.message.DefaultMessage;
import com.sac.model.message.MessageEnvelope;
import com.sac.model.message.MessageEnvelope.Type;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.message.MessageHandlerStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GameEnvelopeHandler implements EnvelopeHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageHandlerRegistry messageHandlerRegistry;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public Type getType() {
        return Type.GAME;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws IOException {
        DefaultMessage defaultMessage = objectMapper.treeToValue(messageEnvelope.getPayload(), DefaultMessage.class);
        if (gameStateService.exists(roomId) || defaultMessage.getType().equals(DefaultMessage.Type.CHAT)) {
            MessageHandlerStrategy handlerStrategy = messageHandlerRegistry.getInstance(defaultMessage.getType());
            handlerStrategy.handle(webSocketSession, defaultMessage, roomId);
        } else
            messageService.sendToSender(webSocketSession, "Game not initialized yet", ServerResponse.Type.ERROR);
    }
}
