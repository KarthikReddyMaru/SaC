package com.sac.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
    private Status status;
    private GameMode gameMode;
    private int totalMovesPlayed;
    private int totalAvailableMoves;

    public enum Status {
        PLAYING, FINISHED
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

    public Position getPlayerPosition(String username, int position) {
        return this
                .getPlayers()
                .stream()
                .filter(player -> player.getUsername().equals(username))
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .getPositions()[position];
    }


    public Position getOpponentPosition(String username, int position) {
        return this
                .getPlayers()
                .stream()
                .filter(player -> !player.getUsername().equals(username))
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .getPositions()[position];
    }

    public Player getPlayer(String username) {
        return this
                .getPlayers()
                .stream()
                .filter(player -> player.getUsername().equals(username))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    public Player getOpponent(String username) {
        return this
                .getPlayers()
                .stream()
                .filter(player -> !player.getUsername().equals(username))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}
