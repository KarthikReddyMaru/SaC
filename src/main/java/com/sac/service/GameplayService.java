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

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        GameMode gameMode = GameMode.fromString(SocketSessionUtil.getGameMode(webSocketSession));

        if (!gameStateService.exists(roomId)) {
            gameStateService.initializeGameState(roomId, List.of(username), gameMode, 2);
            roomConnectionService.addPlayerToRegistry(username, webSocketSession);
            log.info("{} is joined, Game initialized", username);
        } else if (gameStateService.exists(username, roomId)) {
            roomConnectionService.addPlayerToRegistry(username, webSocketSession);
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.gameState(gameStateService.getGameState(roomId)));
            messageService.broadcastMessage(MessageFormat.playerReconnected(username), roomId);
            log.info("{} is re-joined", username);
        } else if (gameStateService.hasEmptySlot(roomId)) {
            if (gameStateService.addPlayerInRoom(username, roomId)) {
                roomConnectionService.addPlayerToRegistry(username, webSocketSession);
                if (!gameStateService.hasEmptySlot(roomId)) {
                    messageService.broadcastMessage(
                            MessageFormat.gameState(gameStateService.getGameState(roomId)), roomId);
                    log.info("{} is joined, GameState is broadcasted", username);
                }
            }
        } else {
            webSocketSession.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    public void tryLeave(WebSocketSession webSocketSession, String roomId) {
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        log.info("{} disconnected", username);
        roomConnectionService.removePlayerFromRegistry(username);
        messageService.broadcastMessage(MessageFormat.playerDisconnected(username), roomId);
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
                 .map(GameState.Player::getUsername)
                 .forEach(roomConnectionService::closePlayerSession);
        gameStateService.removeGameState(roomId);
        log.info("GameState - {}", gameStateService.getGameState(roomId));
    }

}
