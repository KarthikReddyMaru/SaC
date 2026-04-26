package com.sac.service;

import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.strategy.position.PositionSelection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public void initializeGameState(String roomId, List<String> players,
                                    GameMode gameMode, Integer totalPlayers) {
        if (!gameStates.containsKey(roomId)) {
            GameState gameState = GameState
                    .builder()
                    .roomId(roomId)
                    .players(initializePlayers(players))
                    .currentPlayerId(players.getFirst())
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
            gameStates.put(roomId, gameState);
        }
    }

    private List<Player> initializePlayers(List<String> players) {
        return players.stream()
                      .map(username -> {
                          Position[] positions = new Position[playerPositions + 1];
                          for (int i = 0; i <= playerPositions; i++) {
                              positions[i] = Position.builder()
                                                     .positionId(i)
                                                     .actor(null)
                                                     .belongsTo(username)
                                                     .isCapturedByOpponent(false)
                                                     .build();
                          }
                          return new Player(positions, username, 0);
                      })
                      .collect(Collectors.toList());
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

    public boolean exists(String username, String roomId) {
        if (gameStates.containsKey(roomId)) {
            return gameStates.get(roomId)
                             .getPlayers()
                             .stream()
                             .anyMatch(player -> player.getUsername()
                                                       .equals(username));
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

    public boolean addPlayerInRoom(String username, String roomId) {
        Player player = initializePlayers(List.of(username)).getFirst();
        if (hasEmptySlot(roomId)) {
            gameStates.get(roomId).addPlayer(player);
            return true;
        }
        return false;
    }

    public String getOpponentId(String roomId, String playerId) {
        GameState gameState = gameStates.get(roomId);
        return gameState.getPlayers()
                        .stream()
                        .filter(player -> !playerId.equals(player.getUsername()))
                        .findFirst()
                        .orElseThrow(IllegalStateException::new)
                        .getUsername();
    }

    public Position getPlayerPosition(String roomId, String username, int position) {
        return gameStates.get(roomId)
                         .getPlayers()
                         .stream()
                         .filter(player -> player.getUsername()
                                                 .equals(username))
                         .findFirst()
                         .orElseThrow(IllegalStateException::new)
                         .getPositions()[position];
    }

    public Player getPlayer(String roomId, String username) {
        return gameStates.get(roomId)
                         .getPlayers()
                         .stream()
                         .filter(player -> player.getUsername()
                                                 .equals(username))
                         .findFirst()
                         .orElseThrow(IllegalStateException::new);
    }
}
