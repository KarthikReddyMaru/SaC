package com.sac.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.message.DefaultMessage;
import com.sac.model.message.ServerResponse;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RoomConnectionService roomConnectionService;
    private final ObjectMapper objectMapper;

    public void broadcastMessage(String message, String roomId) {
        Set<WebSocketSession> sessions = roomConnectionService.getSessions(roomId);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try { session.sendMessage(new TextMessage(message)); }
                catch (IOException e) { throw new RuntimeException(e); }
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

    public void sendToSender(WebSocketSession session, String message) {
        if (session.isOpen()) {
            ServerResponse response = new ServerResponse(ServerResponse.Type.MESSAGE, "System", message);
            try { session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response))); }
            catch (IOException e) { throw new RuntimeException(e); }
        }
    }

}
