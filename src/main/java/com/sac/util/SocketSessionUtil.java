package com.sac.util;

import lombok.extern.slf4j.Slf4j;
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

    public static String getUserNameFromSession(WebSocketSession webSocketSession) {
        String username = getQueryParamValue(webSocketSession, "username");
        return username != null ? username : UUID.randomUUID().toString().substring(8);
    }

    public static String getGameMode(WebSocketSession webSocketSession) {
        return getQueryParamValue(webSocketSession, "mode");
    }
}
