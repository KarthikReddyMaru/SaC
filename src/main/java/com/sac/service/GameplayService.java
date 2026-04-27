package com.sac.service;

import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import com.sac.util.mode.ClassicPointsUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static com.sac.model.GameState.GameplayStatus.OFFLINE;
import static com.sac.model.GameState.GameplayStatus.PLAYING;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameplayService {

    private final RoomConnectionService roomConnectionService;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    public void tryJoin(WebSocketSession webSocketSession) throws Exception {
        String roomId = SocketSessionUtil.getQueryParamValue(webSocketSession, "roomId");
        if (roomId == null || roomId.isEmpty()) {
            webSocketSession.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        GameStateService.PlayerMetadata metadata = GameStateService.PlayerMetadata.builder()
                                                                                  .clientId(clientId)
                                                                                  .username(username)
                                                                                  .build();
        GameMode gameMode = GameMode.fromString(SocketSessionUtil.getGameMode(webSocketSession));

        if (!gameStateService.exists(roomId)) {
            gameStateService.initializeGameState(roomId, List.of(metadata), gameMode, 2);
            roomConnectionService.addPlayerToRegistry(clientId, webSocketSession);
            log.info("{} is joined, Game initialized", username);
        } else if (gameStateService.exists(clientId, roomId)) {
            roomConnectionService.addPlayerToRegistry(clientId, webSocketSession);
            GameState gameState = gameStateService.getGameState(roomId);
            if (roomConnectionService.isEveryPlayerOnline(roomId) &&
                gameState.getPlayers().size() == gameState.getPlayerCount()) {
                gameState.setGameplayStatus(PLAYING);
                messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
            }
            messageService.broadcastMessage(MessageFormat.playerReconnected(username), roomId);
            log.info("{} is re-joined", username);
        } else if (gameStateService.hasEmptySlot(roomId)) {
            if (gameStateService.addPlayerInRoom(metadata, roomId)) {
                roomConnectionService.addPlayerToRegistry(clientId, webSocketSession);
                if (!gameStateService.hasEmptySlot(roomId)) {
                    GameState gameState = gameStateService.getGameState(roomId);
                    gameState.setGameplayStatus(PLAYING);
                    ClassicPointsUtil.transitionRollToNextPlayer(gameState);
                    messageService.broadcastMessage(
                            MessageFormat.gameState(gameState), roomId);
                    log.info("{} is joined, GameState is broadcasted", username);
                    log.info("TurnOrder: {}", gameStateService.getGameState(roomId)
                                                              .getTurnOrder());
                }
            }
        } else {
            webSocketSession.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    public void tryLeave(WebSocketSession webSocketSession, String roomId) {
        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        log.info("{} disconnected", username);
        GameState gameState = gameStateService.getGameState(roomId);
        gameState.setGameplayStatus(OFFLINE);
        roomConnectionService.removePlayerFromRegistry(clientId);
        messageService.broadcastMessage(MessageFormat.playerDisconnected(username), roomId);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @SneakyThrows
    public void endGame(String roomId, String winner) {
        GameState gameState = gameStateService.getGameState(roomId);
        ClassicPointsUtil.endGameWithWinner(winner, gameState);
        messageService.broadcastMessage(
                MessageFormat.endGameWithWinner(gameState), roomId);
        Thread.sleep(100);
        gameState.getPlayers()
                 .stream()
                 .map(GameState.Player::getClientId)
                 .forEach(roomConnectionService::closePlayerSession);
        gameStateService.removeGameState(roomId);
        log.info("GameState - {}", gameStateService.getGameState(roomId));
    }

}
