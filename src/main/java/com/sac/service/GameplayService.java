package com.sac.service;

import com.sac.factory.GameModeHandlerRegistry;
import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.message.ServerResponse;
import com.sac.strategy.mode.Mode;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
    private final GameModeHandlerRegistry gameModeHandlerRegistry;
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
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
            messageService.broadcastMessage(MessageFormat.chooseMessage(
                    gameStateService.getGameState(roomId).getCurrentPlayerId()),
                    roomId, ServerResponse.Type.INFO);
        }
    }

    @SneakyThrows
    public void postProcessAction(WebSocketSession webSocketSession, String roomId) {
        GameState gameState = gameStateService.getGameState(roomId);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        Mode mode = gameModeHandlerRegistry.getInstance(gameState.getGameMode());
        if (mode.computeWinner(roomId) != null) {
            String winner = SocketSessionUtil.getUserNameFromSession(webSocketSession);
            messageService.broadcastMessage(MessageFormat.endGameWithWinner(winner, gameState), roomId);
            log.info("Game completed, preparing to close connections of room - {}", roomId);
            webSocketSession.close(CloseStatus.NORMAL);
        }
    }
}
