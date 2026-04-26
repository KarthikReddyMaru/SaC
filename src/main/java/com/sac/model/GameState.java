package com.sac.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sac.model.actor.Specialization;
import com.sac.strategy.position.PositionSelection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Deque;
import java.util.List;

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
        PLAYING, FINISHED, OFFLINE
    }

    public enum State {
        ROLL, ACTION_REQUIRED
    }

    @Data
    @AllArgsConstructor
    public static class Player {
        private Position[] positions;
        private String username;
        private String clientId;
        private int points;

        public void addPoints(int points) {
            this.points += points;
        }
    }

    @JsonIgnore
    private Deque<Player> turnOrder;

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public Player getPlayer(String clientId) {
        return this.players.stream()
                           .filter(player -> player.getClientId().equals(clientId))
                           .findFirst()
                           .orElseThrow();
    }

    public Position getPlayerPosition(String clientId, int position) {
        return this.getPlayers()
                   .stream()
                   .filter(player -> player.getClientId()
                                           .equals(clientId))
                   .findFirst()
                   .orElseThrow(IllegalStateException::new)
                   .getPositions()[position];
    }

    public Player cycleNextPlayer() {
        if (turnOrder == null || turnOrder.isEmpty()) {
            throw new IllegalStateException("Cannot get next player: turn order is empty.");
        }
        Player nextPlayer = turnOrder.pollFirst();
        this.turnOrder.addLast(nextPlayer);
        return nextPlayer;
    }
}
