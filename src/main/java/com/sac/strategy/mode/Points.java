package com.sac.strategy.mode;

import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.service.GameStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.sac.model.GameMode.POINTS;

@Component
@RequiredArgsConstructor
public class Points implements Mode {

    private final int pointsToReach = 16;
    private final GameStateService gameStateService;

    @Override
    public String computeWinner(String roomId) {
        GameState gameState = gameStateService.getGameState(roomId);
        List<Player> players = gameState.getPlayers();
        Player winner = players.stream()
                .filter(player -> player.getPoints() >= pointsToReach)
                .findFirst().orElse(null);
        if (winner != null) {
            gameState.setStatus(GameState.Status.FINISHED);
            return winner.getUsername();
        }
        return null;
    }

    @Override
    public GameMode getMode() {
        return POINTS;
    }
}
