package com.sac.config;

import com.sac.handler.LoggingWebSocketDecorator;
import com.sac.handler.RoomConnectionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final RoomConnectionHandler roomConnectionHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new LoggingWebSocketDecorator(roomConnectionHandler), "/ws/room")
                .setAllowedOrigins("*");
    }
}
