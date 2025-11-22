package com.sac.service;

import com.sac.model.GameState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameStateService {

    private final ConcurrentHashMap<String, GameState> gameStates = new ConcurrentHashMap<>();

    public GameState initializeGameState(String roomId, List<WebSocketSession> sessions) {
        if (!gameStates.containsKey(roomId)) {
            GameState gameState = GameState
                    .builder()
                    .roomId(roomId)
                    .board(new String[12][2])
                    .players(sessions)
                    .status(GameState.Status.PLAYING)
                    .playerCount(sessions.size())
                    .totalMovesPlayed(0)
                    .totalAvailableMoves(Integer.MAX_VALUE)
                    .build();
            gameStates.put(roomId, gameState);
        }
        return gameStates.get(roomId);
    }

    public GameState getGameState(String roomId) {
        if (!gameStates.containsKey(roomId))
            log.warn("Game state not found for Room ID: {}", roomId);
        return gameStates.getOrDefault(roomId, null);
    }

    public WebSocketSession getCurrentPlayer(GameState gameState) {
        int totalPlayers = gameState.getPlayerCount();
        int movesPlayed = gameState.getTotalMovesPlayed();
        return gameState.getPlayers().get(totalPlayers % movesPlayed);
    }

    public void endGameState(String roomId) {
        // TODO - Return winner
        gameStates.remove(roomId);
    }
}
