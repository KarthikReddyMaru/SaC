package com.sac.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.factory.EnvelopeHandler;
import com.sac.factory.EnvelopeHandlerRegistry;
import com.sac.model.message.MessageEnvelope;
import com.sac.service.GameplayService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomConnectionHandler extends TextWebSocketHandler {

    private final EnvelopeHandlerRegistry envelopeHandlerRegistry;
    private final GameplayService gameplayService;

    private final ConcurrentHashMap<WebSocketSession, String> sessionRoomMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String roomId = gameplayService.tryJoin(session);
        sessionRoomMap.put(session, roomId);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String roomId = sessionRoomMap.get(session);
        if (roomId != null && gameplayService.tryLeave(session, roomId)) {
            sessionRoomMap.remove(session);
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession webSocketSession, @NonNull TextMessage message) throws Exception {
        String roomId = sessionRoomMap.get(webSocketSession);
        MessageEnvelope messageEnvelope = new ObjectMapper().readValue(message.asBytes(), MessageEnvelope.class);
        EnvelopeHandler envelopeHandler = envelopeHandlerRegistry.getInstance(messageEnvelope.getType());
        envelopeHandler.handle(webSocketSession, messageEnvelope, roomId);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) throws Exception {
        String message = exception.getMessage();
        log.warn("Server stopped: {}", message);
    }

}
