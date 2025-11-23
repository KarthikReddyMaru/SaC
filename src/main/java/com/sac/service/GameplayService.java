package com.sac.service;

import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameplayService {

    private final RoomConnectionService roomConnectionService;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    public String tryJoin(WebSocketSession session) throws Exception {
        String roomId = SocketSessionUtil.getQueryParamValue(session, "roomId");
        if (roomId == null || roomId.isEmpty()) {
            SocketSessionUtil.sendErrorAndClose(session, "Invalid RoomID");
        }
        boolean isJoined = roomConnectionService.tryJoin(roomId, session);
        if (!isJoined) {
            SocketSessionUtil.sendErrorAndClose(session, "Room is full");
        }
        String username = SocketSessionUtil.setUserNameInSession(session);
        messageService.broadcastMessage(String.format("%s is joined", username), roomId);
        tryInitializeGame(roomId);
        return roomId;
    }

    public boolean tryLeave(WebSocketSession session, String roomId) throws IOException {
        boolean isLeft = roomConnectionService.tryRemove(roomId, session);
        if (!isLeft) {
            log.warn("Potential memory leak, Failed to remove closed connection");
        }
        String username = SocketSessionUtil.getUserNameFromSession(session);
        String message = String.format("%s left", username);
        messageService.broadcastMessage(message, roomId);
        cleanUpGame(roomId);
        return true;
    }

    private void tryInitializeGame(String roomId) throws IOException {
        if (roomConnectionService.isFull(roomId) && !gameStateService.exists(roomId)) {
            gameStateService.initializeGameState(roomId, new ArrayList<>(roomConnectionService.getSessions(roomId)));
            messageService.broadcastMessage("Welcome to Shoot and Capture", roomId);
        }
    }

    private void cleanUpGame(String roomId) {
        gameStateService.endGameState(roomId);
    }
}
