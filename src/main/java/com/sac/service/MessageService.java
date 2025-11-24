package com.sac.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.message.DefaultMessage;
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
        for (WebSocketSession session : sessions) {
            if (!session.equals(senderSession) && session.isOpen())
                session.sendMessage(new TextMessage(message));
        }
    }

    public void sendToSender(WebSocketSession session, String message) {
        if (session.isOpen()) {
            try { session.sendMessage(new TextMessage(message)); }
            catch (IOException e) { throw new RuntimeException(e); }
        }
    }

}
