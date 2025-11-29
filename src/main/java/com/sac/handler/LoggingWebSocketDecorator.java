package com.sac.handler;

import com.sac.util.SocketSessionUtil;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;


public class LoggingWebSocketDecorator extends WebSocketHandlerDecorator {

    public LoggingWebSocketDecorator(WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String username = SocketSessionUtil.getUserNameFromSession(session);
        String roomId = SocketSessionUtil.getRoomIdFromSession(session);
        try {
            MDC.put("username", username);
            MDC.put("roomId", roomId);
            super.afterConnectionEstablished(session);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
        String username = SocketSessionUtil.getUserNameFromSession(session);
        String roomId = SocketSessionUtil.getRoomIdFromSession(session);
        try {
            MDC.put("username", username);
            MDC.put("roomId", roomId);
            super.handleMessage(session, message);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        String username = SocketSessionUtil.getUserNameFromSession(session);
        String roomId = SocketSessionUtil.getRoomIdFromSession(session);
        try {
            MDC.put("username", username);
            MDC.put("roomId", roomId);
            super.afterConnectionClosed(session, closeStatus);
        } finally {
            MDC.clear();
        }
    }
}
