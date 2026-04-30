package com.sac.service;

import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import com.sac.util.mode.ClassicPointsUtil;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.*;

import static com.sac.model.GameState.GameplayStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameplayService {

    private final RoomConnectionService roomConnectionService;
    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final MeterRegistry meterRegistry;

    private final Map<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    @WithSpan("room.join")
    public void tryJoin(WebSocketSession webSocketSession) throws Exception {
        String roomId = SocketSessionUtil.getQueryParamValue(webSocketSession, "roomId");
        if (roomId == null || roomId.isEmpty()) {
            webSocketSession.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        int totalPlayers = SocketSessionUtil.getRoomSizeFromSession(webSocketSession);
        GameStateService.PlayerMetadata metadata = GameStateService.PlayerMetadata.builder()
                                                                                  .clientId(clientId)
                                                                                  .username(username)
                                                                                  .build();
        GameMode gameMode = GameMode.fromString(SocketSessionUtil.getGameMode(webSocketSession));
        GameState gameState = gameStateService.initializeGameState(roomId, gameMode, totalPlayers);

        if (gameStateService.exists(clientId, roomId)) {
            if (roomConnectionService.exists(clientId)) {
                roomConnectionService.addPlayerToRegistry(clientId, webSocketSession);
                if (gameState.getGameplayStatus().equals(PLAYING)) {
                    messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
                    return;
                }
            }
            if (!endTimer(clientId, roomId)) {
                webSocketSession.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }
            roomConnectionService.addPlayerToRegistry(clientId, webSocketSession);
            if (roomConnectionService.isEveryPlayerOnline(roomId) &&
                gameState.getPlayers()
                         .size() == gameState.getPlayerCount()) {
                gameState.setGameplayStatus(PLAYING);
                messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
            }
            messageService.broadcastMessage(MessageFormat.playerReconnected(username), roomId);
        } else if (gameStateService.addPlayerInRoom(metadata, roomId)) {
            roomConnectionService.addPlayerToRegistry(clientId, webSocketSession);
            log.info("{} is joined", username);
            checkAndStartGame(roomId);
        } else {
            log.warn("Disconnecting player because {} is full", roomId);
            webSocketSession.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @WithSpan("room.leave")
    public void tryLeave(WebSocketSession webSocketSession, String roomId) {
        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        roomConnectionService.removePlayerFromRegistry(clientId);
        log.info("{} disconnected, Entry in user registry - {}", username, roomConnectionService.exists(clientId));
        GameState gameState = gameStateService.getGameState(roomId);
        if (gameState == null || gameState.getGameplayStatus().equals(FINISHED))
            return;
        gameState.setGameplayStatus(OFFLINE);
        if (gameStateService.exists(clientId, roomId))
            startTimer(clientId, roomId);
        messageService.broadcastMessage(MessageFormat.playerDisconnected(username), roomId);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    private void checkAndStartGame(String roomId) {
        GameState gameState = gameStateService.getGameState(roomId);
        if (roomConnectionService.isEveryPlayerOnline(roomId) &&
            gameState.getPlayers()
                     .size() == gameState.getPlayerCount()) {

            gameState.setGameplayStatus(PLAYING);
            ClassicPointsUtil.transitionRollToNextPlayer(gameState);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
            log.info("GameState is broadcasted");
            log.info("Transitioned roll to {}", gameStateService.getUsernameFromId(gameState.getCurrentPlayerId(), roomId));
        }
    }

    public void endGame(String roomId, String winner) {
        if (!winner.equals("NONE"))
            log.info("Game completed, Winner: {}", gameStateService.getUsernameFromId(winner, roomId));
        GameState gameState = gameStateService.getGameState(roomId);
        if (gameState != null) {
            ClassicPointsUtil.endGameWithWinner(winner, gameState);
            messageService.broadcastMessage(
                    MessageFormat.endGameWithWinner(gameState), roomId);
            gameState.getPlayers()
                     .stream()
                     .map(GameState.Player::getClientId)
                     .forEach(roomConnectionService::closePlayerSession);
            gameStateService.removeGameState(roomId);
            log.info("GameState of {} - {}", roomId, gameStateService.getGameState(roomId));
        }
    }

    public void startTimer(String clientId, String roomId) {
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(() -> {
            if (timers.remove(clientId) == null) {
                log.info("Looks like {} re-joined, {} is alive",
                         gameStateService.getUsernameFromId(clientId, roomId),
                         roomId);
                return;
            }
            log.info("Timeout, clearing gameState of {}", roomId);
            endGame(roomId, "NONE");
        }, 60, TimeUnit.SECONDS);
        timers.put(clientId, scheduledFuture);
        log.warn("Timer started to clear gameState");
    }

    public boolean endTimer(String clientId, String roomId) {
        ScheduledFuture<?> scheduledFuture = timers.remove(clientId);
        String username = gameStateService.getUsernameFromId(clientId, roomId);
        if (scheduledFuture == null) {
            log.info("{} joined, but gameState is already cleared", username);
            return false;
        }
        scheduledFuture.cancel(false);
        log.info("Timer stopped because {} is re-joined", username);
        return true;
    }

    @PreDestroy
    private void shutdown() {
        scheduledExecutorService.shutdown();
    }

    @PostConstruct
    private void initMetrics() {
        Gauge.builder("Sac.active.timers", timers, Map::size)
                .description("Number of game states about to be removed")
                .register(meterRegistry);
    }

}
