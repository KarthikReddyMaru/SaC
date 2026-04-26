package com.sac.service;

import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.strategy.position.PositionSelection;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.sac.model.GameState.GameplayStatus;
import static com.sac.model.GameState.State.ROLL;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameStateService {

    private final ConcurrentHashMap<String, GameState> gameStates = new ConcurrentHashMap<>();
    @Value("${player.total_positions}")
    private int playerPositions;

    public void initializeGameState(String roomId, List<PlayerMetadata> players,
                                    GameMode gameMode, Integer totalPlayers) {
        if (!gameStates.containsKey(roomId)) {
            GameState gameState = GameState
                    .builder()
                    .roomId(roomId)
                    .players(initializePlayers(players))
                    .actionPending(false)
                    .actionPendingOn(null)
                    .actionPendingOnActor(null)
                    .gameplayStatus(GameplayStatus.PLAYING)
                    .state(ROLL)
                    .gameMode(gameMode)
                    .positionSelection(PositionSelection.ROLL)
                    .playerCount(totalPlayers)
                    .winner(null)
                    .totalMovesPlayed(0)
                    .totalAvailableMoves(Integer.MAX_VALUE)
                    .build();

            gameState.setTurnOrder(initializeTurnOrder(gameState));
            gameStates.put(roomId, gameState);
        }
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

    private ArrayDeque<Player> initializeTurnOrder(GameState gameState) {
        List<Player> players = gameState.getPlayers();
        ArrayDeque<Player> turnOrder = new ArrayDeque<>();
        players.forEach(turnOrder::addLast);
        return turnOrder;
    }

    public GameState getGameState(String roomId) {
        if (!gameStates.containsKey(roomId))
            log.warn("Game state not found for Room ID: {}", roomId);
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
        if (hasEmptySlot(roomId)) {
            gameStates.get(roomId).addPlayer(player);
            gameStates.get(roomId).getTurnOrder().addLast(player);
            return true;
        }
        return false;
    }

    public String getUsernameFromId(String clientId, String roomId) {
        return gameStates.get(roomId)
                .getPlayer(clientId).getUsername();
    }

    @Builder
    public static class PlayerMetadata {
        private String username;
        private String clientId;
    }
}
