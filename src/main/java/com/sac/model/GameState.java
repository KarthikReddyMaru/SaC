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
    private int actionPendingOn;
    private Status status;
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
    }
}
