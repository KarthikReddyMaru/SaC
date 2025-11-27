package com.sac.service;

import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.message.ServerResponse;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameplayService {

    private final RoomConnectionService roomConnectionService;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    public String tryJoin(WebSocketSession webSocketSession) throws Exception {
        String roomId = SocketSessionUtil.getQueryParamValue(webSocketSession, "roomId");
        if (roomId == null || roomId.isEmpty()) {
            SocketSessionUtil.sendErrorAndClose(webSocketSession, "Invalid RoomID");
            throw new IllegalArgumentException();
        }
        boolean isJoined = roomConnectionService.tryJoin(roomId, webSocketSession);
        if (!isJoined) {
            SocketSessionUtil.sendErrorAndClose(webSocketSession, "Room is full");
            throw new IllegalArgumentException();
        }
        String username = SocketSessionUtil.setUserInSession(webSocketSession);
        messageService.broadcastMessage(
                String.format("%s is joined", username),
                roomId, ServerResponse.Type.INFO);
        GameMode gameMode = GameMode.fromString(SocketSessionUtil.getGameMode(webSocketSession));
        tryInitializeGame(roomId, gameMode);
        return roomId;
    }

    public void tryLeave(WebSocketSession webSocketSession, String roomId) throws Exception {
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        log.info("{} arrived for removal, GameState - {}", username, gameStateService.exists(roomId));
        if (roomConnectionService.tryRemove(roomId, username))
            gameStateService.endGameState(roomId);
        log.info("{} left, GameState - {}", username, gameStateService.exists(roomId));
    }

    private void tryInitializeGame(String roomId, GameMode gameMode) {
        if (roomConnectionService.isFull(roomId) && !gameStateService.exists(roomId)) {
            GameState gameState = gameStateService.initializeGameState(roomId, new ArrayList<>(roomConnectionService.getPlayers(roomId)), gameMode);
            messageService.broadcastMessage("Welcome to Shoot and Capture", roomId);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
            messageService.broadcastMessage(
                    String.format("%s gets to pick a number now",
                            gameStateService.getGameState(roomId).getCurrentPlayerId()),
                    roomId, ServerResponse.Type.INFO);
        }
    }
}
