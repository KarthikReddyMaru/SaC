package com.sac.service;

import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.message.ServerResponse;
import com.sac.strategy.position.PositionSelection;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
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
            webSocketSession.close(CloseStatus.NOT_ACCEPTABLE);
            return null;
        }
        boolean isJoined = roomConnectionService.tryJoin(roomId, webSocketSession);
        if (!isJoined) {
            webSocketSession.close(CloseStatus.NOT_ACCEPTABLE);
            return null;
        }
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        log.info("{} is joined", username);
        messageService.broadcastMessage(
                String.format("%s is joined", username),
                roomId, ServerResponse.Type.INFO);
        GameMode gameMode = GameMode.fromString(SocketSessionUtil.getGameMode(webSocketSession));
        tryInitializeGame(roomId, gameMode);
        return roomId;
    }

    public void tryLeave(WebSocketSession webSocketSession, String roomId) throws Exception {
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        log.info("RoomId - {}, clearing gameState...", roomId);
        if (roomConnectionService.tryRemove(roomId, username))
            gameStateService.endGameState(roomId);
        log.info("RoomId - {}, GameState in memory - {}", roomId, gameStateService.exists(roomId));
    }

    private void tryInitializeGame(String roomId, GameMode gameMode) {
        if (roomConnectionService.isFull(roomId) && !gameStateService.exists(roomId)) {
            GameState gameState = gameStateService.initializeGameState(roomId, new ArrayList<>(roomConnectionService.getPlayers(roomId)), gameMode);
            log.info("GameState initialized");
            postGameInitialization(roomId, gameState);
        }
    }

    private void postGameInitialization(String roomId, GameState gameState) {
        if (gameState.getPositionSelection().equals(PositionSelection.ROLL)) {
            String currentPlayerId = gameStateService.getGameState(roomId)
                                                     .getCurrentPlayerId();
            WebSocketSession currentPlayerSession = roomConnectionService.getPlayerSession(roomId, currentPlayerId);
            messageService.broadcastMessage(MessageFormat.rollMessage(currentPlayerId), roomId);
            messageService.sendToSender(currentPlayerSession, MessageFormat.rollAction());
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        }
    }
}
