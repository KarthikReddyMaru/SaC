package com.sac.service;

import com.sac.model.GameState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomConnectionService {

    @Getter
    private final Map<String, WebSocketSession> userRegistry = new ConcurrentHashMap<>();
    private final GameStateService gameStateService;

    public Set<WebSocketSession> getSessions(String roomId) {
        return getPlayers(roomId)
                    .stream()
                    .map(userRegistry::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> getPlayers(String roomId) {
        return gameStateService.getGameState(roomId)
                               .getPlayers()
                               .stream()
                               .map(GameState.Player::getClientId)
                               .collect(Collectors.toUnmodifiableSet());
    }

    public WebSocketSession getPlayerSession(String username) {
        WebSocketSession webSocketSession =  userRegistry.getOrDefault(username, null);
        if (webSocketSession != null && webSocketSession.isOpen()) {
            return webSocketSession;
        }
        return null;
    }

    public void addPlayerToRegistry(String username, WebSocketSession webSocketSession) throws IOException {
        if (this.userRegistry.containsKey(username))
            this.userRegistry.get(username).close(CloseStatus.NOT_ACCEPTABLE);
        this.userRegistry.put(username, webSocketSession);
    }

    public void removePlayerFromRegistry(String username) {
        this.userRegistry.remove(username);
        log.info("User registry: {}", userRegistry);
    }

    public void closePlayerSession(String username) {
        WebSocketSession webSocketSession = this.userRegistry.getOrDefault(username, null);
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
