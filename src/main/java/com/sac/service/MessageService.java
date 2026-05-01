package com.sac.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.message.ServerResponse;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final RoomConnectionService roomConnectionService;
    private final ObjectMapper objectMapper;

    public void broadcastMessage(String message, String roomId) {
        Set<WebSocketSession> sessions = roomConnectionService.getSessions(roomId);
        for (WebSocketSession session : sessions) {
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.warn("Exception occurred while broadcasting: {}", e.getMessage());
                }
            }
        }
    }

    public void sendMessage(WebSocketSession senderSession, String message, String roomId) throws IOException {
        Set<WebSocketSession> sessions = roomConnectionService.getSessions(roomId);
        String username = SocketSessionUtil.getUserNameFromSession(senderSession);
        for (WebSocketSession session : sessions) {
            if (!session.equals(senderSession) && session.isOpen()) {
                ServerResponse response = new ServerResponse(ServerResponse.Type.MESSAGE, username, message);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
        }
    }

    public void sendRawPayload(WebSocketSession session, String message) {
        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
