package com.sac.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Builder
@Data
public class GameState {

    private String roomId;
    private Position[][] board;
    private List<WebSocketSession> players;
    private int playerCount;
    private Status status;
    private int totalMovesPlayed;
    private int totalAvailableMoves;

    public enum Status {
        PLAYING, FINISHED
    }
}
