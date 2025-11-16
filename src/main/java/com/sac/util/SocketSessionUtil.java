package com.sac.util;

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

}
