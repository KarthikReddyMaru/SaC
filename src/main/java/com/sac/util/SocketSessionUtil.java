package com.sac.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;
import java.util.UUID;

@Slf4j
public class SocketSessionUtil {

    public static String getQueryParamValue(WebSocketSession webSocketSession, String target) {
        String query = Objects.requireNonNull(webSocketSession.getUri()).getQuery();
        target = target.concat("=");
        if (query != null && query.contains(target)) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith(target))
                    return param.substring(target.length());
            }
        }
        return null;
    }

    public static void sendErrorAndClose(WebSocketSession session, String msg) throws Exception {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(msg));
            session.close(CloseStatus.POLICY_VIOLATION);
            log.info("{}'s session closed due to {}", session.getAttributes().get("username"), msg);
        }
    }

    public static String setUserInSession(WebSocketSession webSocketSession) {
        String username = getUserNameFromSession(webSocketSession);
        webSocketSession.getAttributes().put("username", username);
        return username;
    }

    public static String getUserNameFromSession(WebSocketSession webSocketSession) {
        String username = getQueryParamValue(webSocketSession, "username");
        return username != null ? username : UUID.randomUUID().toString().substring(8);
    }
}
