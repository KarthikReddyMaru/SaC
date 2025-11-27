package com.sac.service;

import com.sac.util.SocketSessionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomConnectionService {

    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, WebSocketSession> userRegistry = new ConcurrentHashMap<>();

    @Value("${room.size}")
    private int maxRoomSize;

    public boolean tryJoin(String roomId, WebSocketSession webSocketSession) throws Exception {
        int currRoomSize = rooms.getOrDefault(roomId, Collections.emptySet()).size();
        if (currRoomSize == maxRoomSize) return false;
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        return addUserToRoom(roomId, webSocketSession, username);
    }

    private boolean addUserToRoom(String roomId, WebSocketSession webSocketSession, String username) throws Exception {
        WebSocketSession existing = userRegistry.putIfAbsent(username, webSocketSession);
        if (existing != null) {
            webSocketSession.close(CloseStatus.POLICY_VIOLATION);
            return false;
        }
        rooms.computeIfAbsent(roomId, (room) -> Collections.synchronizedSet(new HashSet<>()));
        rooms.get(roomId).add(username);
        return true;
    }

    public boolean tryRemove(String roomId, String username) throws Exception {
        log.info("{} arrived for removal, rooms - {}, userRegistry - {}", username, rooms, userRegistry);
        Set<String> players = rooms.get(roomId);
        if (players == null) return false;

        players.remove(username);
        userRegistry.remove(username);

        // Close all remaining players' sessions and cleanup
        for (String player : new ArrayList<>(players)) {
            WebSocketSession session = userRegistry.remove(player);
            if (session != null && session.isOpen()) {
                session.close(CloseStatus.POLICY_VIOLATION);
            }
        }

        rooms.remove(roomId);
        log.info("{} left, rooms - {}, userRegistry - {}", username, rooms, userRegistry);
        return true;
    }


    public Set<WebSocketSession> getSessions(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet())
                .stream()
                .map(userRegistry::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> getPlayers(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }

    public int getRoomSize(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet()).size();
    }

    public boolean isFull(String roomId) { return getRoomSize(roomId) == maxRoomSize; }
}
