package com.sac.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.message.DefaultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RoomConnectionService roomConnectionService;
    private final ObjectMapper objectMapper;

    public void broadcastMessage(DefaultMessage message, String roomId) throws IOException {
        String objToJson = objectMapper.writeValueAsString(message);
        broadcastMessage(objToJson, roomId);
    }

    public void broadcastMessage(String message, String roomId) throws IOException {
        Set<WebSocketSession> sessions = roomConnectionService.getSessions(roomId);
        for (WebSocketSession session : sessions) {
            if (session.isOpen())
                session.sendMessage(new TextMessage(message));
        }
    }

    public void sendMessage(WebSocketSession webSocketSession, DefaultMessage message, String roomId) throws IOException {
        String objToJson = objectMapper.writeValueAsString(message);
        sendMessage(webSocketSession, objToJson, roomId);
    }

    public void sendMessage(WebSocketSession senderSession, String message, String roomId) throws IOException {
        Set<WebSocketSession> sessions = roomConnectionService.getSessions(roomId);
        for (WebSocketSession session : sessions) {
            if (!session.equals(senderSession) && session.isOpen())
                session.sendMessage(new TextMessage(message));
        }
    }

    public void sendToSender(WebSocketSession session, String message) throws IOException {
        if (session.isOpen())
            session.sendMessage(new TextMessage(message));
    }

}
