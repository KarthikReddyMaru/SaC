package com.sac.handler;

import com.sac.service.RoomConnectionService;
import com.sac.util.SocketSessionUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomConnectionHandler extends TextWebSocketHandler {

    private final RoomConnectionService roomConnectionService;
    private final ConcurrentHashMap<WebSocketSession, String> sessionRoomMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String roomId = SocketSessionUtil.getQueryParamValue(session, "roomId");
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
        broadcastMessage(message, roomId);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String roomId = sessionRoomMap.get(session);
        if (roomId != null) {
            boolean isLeft = roomConnectionService.tryRemove(roomId, session);
            if (!isLeft) {
                log.warn("Potential memory leak, Failed to remove close connection");
                return;
            }
            sessionRoomMap.remove(session);
            String message = "Player left";
            broadcastMessage(message, roomId);
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String roomId = sessionRoomMap.get(session);
        broadcastMessage(message.getPayload(), roomId);
    }

    public void sendErrorAndClose(WebSocketSession session, String msg) throws Exception {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(msg));
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    public void broadcastMessage(String message, String roomId) throws IOException {
        Set<WebSocketSession> sessions = roomConnectionService.getSessions(roomId);
        for (WebSocketSession session : sessions) {
            if (session.isOpen())
                session.sendMessage(new TextMessage(message));
        }
    }
}
