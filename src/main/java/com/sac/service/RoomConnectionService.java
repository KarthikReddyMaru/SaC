package com.sac.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RoomConnectionService {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Value("${room.size}")
    private int maxRoomSize;

    public synchronized boolean tryJoin(String roomId, WebSocketSession webSocketSession) {
        rooms.computeIfAbsent(roomId, (room) -> Collections.synchronizedSet(new HashSet<>()));
        int currRoomSize = rooms.get(roomId).size();
        if (currRoomSize == maxRoomSize) return false;
        rooms.get(roomId).add(webSocketSession);
        return true;
    }

    public synchronized boolean tryRemove(String roomId, WebSocketSession webSocketSession) {
        if (!rooms.containsKey(roomId)) return false;
        rooms.get(roomId).remove(webSocketSession);
        int currRoomSize = rooms.get(roomId).size();
        if (currRoomSize == 0)
            rooms.remove(roomId);
        return true;
    }

    public Set<WebSocketSession> getSessions(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }

    public int getRoomSize(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet()).size();
    }

}
