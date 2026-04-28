package com.sac.handler;

import com.sac.util.SocketSessionUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Component
public class LoggingWebSocketDecorator extends WebSocketHandlerDecorator {

    public LoggingWebSocketDecorator(@Qualifier("roomConnectionHandler") WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {

        String username = SocketSessionUtil.getUserNameFromSession(session);
        String roomId = SocketSessionUtil.getRoomIdFromSession(session);
        String clientId = SocketSessionUtil.getClientIdFromSession(session);

        Span span = Span.current();

        try {

            MDC.put("username", username);
            MDC.put("roomId", roomId);
            MDC.put("clientId", clientId);

            span.setAttribute("username", username);
            span.setAttribute("roomId", roomId);
            span.setAttribute("clientId", clientId);

            super.afterConnectionEstablished(session);
        } finally {
            MDC.clear();
        }
    }

    @Override
    @WithSpan("ws.onmessage")
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {

        String username = SocketSessionUtil.getUserNameFromSession(session);
        String roomId = SocketSessionUtil.getRoomIdFromSession(session);
        String clientId = SocketSessionUtil.getClientIdFromSession(session);

        Span span = Span.current();

        try {

            MDC.put("username", username);
            MDC.put("roomId", roomId);
            MDC.put("clientId", clientId);

            span.setAttribute("username", username);
            span.setAttribute("roomId", roomId);
            span.setAttribute("clientId", clientId);

            super.handleMessage(session, message);

        } finally {
            MDC.clear();
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        String username = SocketSessionUtil.getUserNameFromSession(session);
        String roomId = SocketSessionUtil.getRoomIdFromSession(session);
        String clientId = SocketSessionUtil.getClientIdFromSession(session);

        Span span = Span.current();

        try {

            MDC.put("username", username);
            MDC.put("roomId", roomId);
            MDC.put("clientId", clientId);

            span.setAttribute("username", username);
            span.setAttribute("roomId", roomId);
            span.setAttribute("clientId", clientId);

            super.afterConnectionClosed(session, closeStatus);

        } finally {
            MDC.clear();
        }
    }
}
