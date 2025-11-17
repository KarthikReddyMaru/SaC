package com.sac.util;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

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
        }
    }
}
