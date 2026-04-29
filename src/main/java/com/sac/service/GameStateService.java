package com.sac.service;

import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.strategy.position.PositionSelection;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.sac.model.GameState.GameplayStatus.INIT;
import static com.sac.model.GameState.State.ROLL;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameStateService {

    @Value("${player.total_positions}")
    private int playerPositions;
    private final ConcurrentHashMap<String, GameState> gameStates = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public GameState initializeGameState(String roomId, GameMode gameMode, Integer totalPlayers) {
        return gameStates.computeIfAbsent(roomId, id -> {
            GameState gameState = GameState
                    .builder()
                    .roomId(roomId)
                    .players(new CopyOnWriteArrayList<>())
                    .actionPending(false)
                    .actionPendingOn(null)
                    .actionPendingOnActor(null)
                    .gameplayStatus(INIT)
                    .state(ROLL)
                    .gameMode(gameMode)
                    .positionSelection(PositionSelection.ROLL)
                    .playerCount(totalPlayers)
                    .winner(null)
                    .totalMovesPlayed(0)
                    .totalAvailableMoves(Integer.MAX_VALUE)
                    .build();

            gameState.setTurnOrder(new ConcurrentLinkedDeque<>());
            return gameState;
        });
    }

    private List<Player> initializePlayers(List<PlayerMetadata> players) {
        return players.stream()
                      .map(metadata -> {
                          Position[] positions = new Position[playerPositions + 1];
                          for (int i = 0; i <= playerPositions; i++) {
                              positions[i] = Position.builder()
                                                     .positionId(i)
                                                     .actor(null)
                                                     .belongsTo(metadata.username)
                                                     .isCapturedByOpponent(false)
                                                     .build();
                          }
                          return new Player(positions, metadata.username, metadata.clientId, 0);
                      })
                      .collect(Collectors.toList());
    }

    public GameState getGameState(String roomId) {
        return gameStates.getOrDefault(roomId, null);
    }

    public void removeGameState(String roomId) {
        gameStates.remove(roomId);
    }

    public boolean exists(String roomId) {
        return gameStates.containsKey(roomId);
    }

    public boolean exists(String clientId, String roomId) {
        if (gameStates.containsKey(roomId)) {
            return gameStates.get(roomId)
                             .getPlayers()
                             .stream()
                             .anyMatch(player -> player.getClientId()
                                                       .equals(clientId));
        }
        return false;
    }

    public boolean hasEmptySlot(String roomId) {
        if (gameStates.containsKey(roomId)) {
            return gameStates.get(roomId)
                             .getPlayers()
                             .size() < gameStates.get(roomId)
                                                 .getPlayerCount();
        }
        return true;
    }

    public boolean addPlayerInRoom(PlayerMetadata metadata, String roomId) {
        Player player = initializePlayers(List.of(metadata)).getFirst();
        GameState gameState = gameStates.get(roomId);
        synchronized (gameState) {
            if (hasEmptySlot(roomId)) {
                gameStates.get(roomId)
                          .addPlayer(player);
                gameStates.get(roomId)
                          .getTurnOrder()
                          .addLast(player);
                return true;
            }
        }
        return false;
    }

    public String getUsernameFromId(String clientId, String roomId) {
        return gameStates.get(roomId)
                         .getPlayer(clientId)
                         .getUsername();
    }

    @Builder
    public static class PlayerMetadata {
        private String username;
        private String clientId;
    }

    @PostConstruct
    public void initMetric() {
        Gauge.builder("SaC.rooms.active", gameStates, ConcurrentHashMap::size)
                .description("Current number of active game state rooms")
                .register(meterRegistry);
    }
}
