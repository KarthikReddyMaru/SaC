package com.sac.model;

import com.sac.model.actor.Specialization;
import com.sac.strategy.position.PositionSelection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

import static com.sac.model.GameState.GameplayStatus.FINISHED;

@Data
@Builder(toBuilder = true)
public class GameState {

    private String roomId;
    private List<Player> players;
    private int playerCount;
    private String currentPlayerId;
    private boolean actionPending;
    private Integer actionPendingOn;
    private boolean isWildCardActive;
    private Specialization actionPendingOnActor;
    private GameplayStatus gameplayStatus;
    private State state;
    private GameMode gameMode;
    private PositionSelection positionSelection;
    private String winner;
    private int totalMovesPlayed;
    private int totalAvailableMoves;

    public enum GameplayStatus {
        PLAYING, FINISHED
    }

    public enum State {
        ROLL, ACTION_REQUIRED
    }

    @Data
    @AllArgsConstructor
    public static class Player {
        private Position[] positions;
        private String username;
        private int points;

        public void addPoints(int points) {
            this.points += points;
        }
    }

    public Player getPlayer(String username) {
        return this.players.stream()
                           .filter(player -> player.getUsername().equals(username))
                           .findFirst()
                           .orElseThrow();
    }

    public Position getPlayerPosition(String username, int position) {
        return this.getPlayers()
                   .stream()
                   .filter(player -> player.getUsername()
                                           .equals(username))
                   .findFirst()
                   .orElseThrow(IllegalStateException::new)
                   .getPositions()[position];
    }

    public Position getOpponentPosition(String username, int position) {
        return this.getPlayers()
                   .stream()
                   .filter(player -> !player.getUsername()
                                            .equals(username))
                   .findFirst()
                   .orElseThrow(IllegalStateException::new)
                   .getPositions()[position];
    }

    public Player getOpponent(String username) {
        return this.getPlayers()
                   .stream()
                   .filter(player -> !player.getUsername()
                                            .equals(username))
                   .findFirst()
                   .orElseThrow(IllegalStateException::new);
    }
}
