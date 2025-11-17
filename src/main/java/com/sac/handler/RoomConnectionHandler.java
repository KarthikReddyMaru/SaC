package com.sac.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.factory.MessageHandlerFactory;
import com.sac.model.Message;
import com.sac.service.MessageService;
import com.sac.service.RoomConnectionService;
import com.sac.strategy.message.MessageHandlerStrategy;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.sac.util.SocketSessionUtil.getQueryParamValue;
import static com.sac.util.SocketSessionUtil.sendErrorAndClose;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomConnectionHandler extends TextWebSocketHandler {

    private final RoomConnectionService roomConnectionService;
    private final MessageService messageService;
    private final MessageHandlerFactory messageHandlerFactory;
    private final AtomicBoolean shutdownStatus = new AtomicBoolean(false);
    private final ConcurrentHashMap<WebSocketSession, String> sessionRoomMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String roomId = getQueryParamValue(session, "roomId");
        if (roomId == null || roomId.isEmpty()) {
            log.info("Invalid room Id");
            sendErrorAndClose(session, "Invalid RoomID");
            return;
        }
        boolean isJoined = roomConnectionService.tryJoin(roomId, session);
        if (!isJoined) {
            log.info("Room is full");
            sendErrorAndClose(session, "Room is full");
            return;
        }
        String message = "Player joined";
        sessionRoomMap.put(session, roomId);
        messageService.broadcastMessage(message, roomId);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String roomId = sessionRoomMap.get(session);
        if (roomId != null) {
            boolean isLeft = roomConnectionService.tryRemove(roomId, session);
            if (!isLeft) {
                log.warn("Potential memory leak, Failed to remove closed connection");
                return;
            }
            if (!checkShutDownStatus(status)) {
                sessionRoomMap.remove(session);
                String message = "Player left";
                messageService.broadcastMessage(message, roomId);
            }
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String roomId = sessionRoomMap.get(session);
        Message parsedMessage = new ObjectMapper().readValue(message.asBytes(), Message.class);
        MessageHandlerStrategy handlerStrategy = messageHandlerFactory.getInstance(parsedMessage.getType());
        handlerStrategy.handle(session, parsedMessage, roomId);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) throws Exception {
        String message = exception.getMessage();
        log.warn("Server stopped: {}", message);
    }


    private boolean checkShutDownStatus(@NonNull CloseStatus status) {
        shutdownStatus.set(
                status == CloseStatus.GOING_AWAY ||
                status == CloseStatus.SERVICE_RESTARTED ||
                status == CloseStatus.SERVER_ERROR
        );
        log.warn("Server is going down...");
        return shutdownStatus.get();
    }
}
