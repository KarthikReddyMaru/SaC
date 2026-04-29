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

    public void addPlayerToRegistry(String clientId, WebSocketSession webSocketSession) throws IOException {
        WebSocketSession oldSession = this.userRegistry.put(clientId, webSocketSession);
        if (oldSession != null && oldSession.isOpen())
            oldSession.close(CloseStatus.NOT_ACCEPTABLE);
    }

    public void removePlayerFromRegistry(String username) {
        this.userRegistry.remove(username);
    }

    public void closePlayerSession(String username) {
        WebSocketSession webSocketSession = this.userRegistry.getOrDefault(username, null);
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close(CloseStatus.NORMAL);
            } catch (IOException ignored) {}
        }
    }

    public boolean isEveryPlayerOnline(String roomId) {
        return getPlayers(roomId).stream().allMatch(this.userRegistry::containsKey);
    }

    public boolean isAnyPlayerOnline(String roomId) {
        return getPlayers(roomId).stream().anyMatch(this.userRegistry::containsKey);
    }

}
