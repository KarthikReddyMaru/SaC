package com.sac.service;

import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.Position;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameStateService {

    private final ConcurrentHashMap<String, GameState> gameStates = new ConcurrentHashMap<>();

    public void initializeGameState(String roomId, List<String> players) {
        if (!gameStates.containsKey(roomId)) {
            GameState gameState = GameState
                    .builder()
                    .roomId(roomId)
                    .players(initializePlayers(players))
                    .currentPlayerId(players.getFirst())
                    .actionPending(false)
                    .status(GameState.Status.PLAYING)
                    .playerCount(players.size())
                    .totalMovesPlayed(0)
                    .totalAvailableMoves(Integer.MAX_VALUE)
                    .build();
            gameStates.put(roomId, gameState);
        }
    }

    private List<Player> initializePlayers(List<String> players) {
        int playerPositions = 6;
        return players.stream()
                .map(username -> {
                    Position[] positions = new Position[playerPositions];
                    for (int i = 0; i < playerPositions; i++) {
                        positions[i] = Position.builder()
                                .positionId(i)
                                .belongsTo(username)
                                .isCapturedByOpponent(false)
                                .build();
                    }
                    return new Player(positions, username);
                }).toList();
    }


    public GameState getGameState(String roomId) {
        if (!gameStates.containsKey(roomId))
            log.warn("Game state not found for Room ID: {}", roomId);
        return gameStates.getOrDefault(roomId, null);
    }

    public void endGameState(String roomId) {
        // TODO - Return winner
        gameStates.remove(roomId);
    }

    public boolean exists(String roomId) {
        return gameStates.containsKey(roomId);
    }

    public void setActionPendingOn(String roomId, int position) {
        GameState gameState = gameStates.get(roomId);
        gameState.setActionPending(true);
        gameState.setActionPendingOn(position);
    }

    public void setCurrentPlayerId(String roomId, String playerId) {
        GameState gameState = gameStates.get(roomId);
        gameState.setCurrentPlayerId(playerId);
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
}
