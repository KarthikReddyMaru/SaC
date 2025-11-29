package com.sac.service;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import java.util.List;

public class GameRendererService {

    private static final int WIDTH = 62;
    private static final String H_BORDER = "═".repeat(WIDTH - 2);
    private static final String GRID_GAP = "                "; // 16 spaces
    private static final String GRID_MARGIN = "          ";    // 10 spaces

    private GameRendererService() {}

    public static String render(GameState gameState) {
        StringBuilder sb = new StringBuilder();

        // --- HEADER ---
        sb.append("╔").append(H_BORDER).append("╗\n");
        sb.append(centerLine("SaC ARENA")).append("\n");
        sb.append("╠").append(H_BORDER).append("╣\n");

        String safeRoom = truncate(gameState.getRoomId(), 12);
        String safePlayer = truncate(gameState.getCurrentPlayerId() != null ? gameState.getCurrentPlayerId() : "-", 10);

        String info = getInfo(gameState, safePlayer, safeRoom);

        sb.append(centerLine(info)).append("\n");
        sb.append("╚").append(H_BORDER).append("╝\n\n");

        // --- PLAYERS & BOARD ---
        List<GameState.Player> players = gameState.getPlayers();
        if (players == null || players.size() < 2) {
            return sb.append("\n  [Waiting for players...]\n").toString();
        }

        GameState.Player p1 = players.get(0);
        GameState.Player p2 = players.get(1);

        // Player Column Headers
        String p1Header = truncate(p1.getUsername(), 10) + " (P1)";
        String p2Header = truncate(p2.getUsername(), 10) + " (P2)";

        sb.append(GRID_MARGIN)
                .append(centerText(p1Header, 13))
                .append(GRID_GAP)
                .append(centerText(p2Header, 13))
                .append("\n");

        // Grid Top
        sb.append(GRID_MARGIN).append("┌───────────┐").append(GRID_GAP).append("┌───────────┐\n");

        int maxRows = Math.max(
                p1.getPositions() != null ? p1.getPositions().length : 0,
                p2.getPositions() != null ? p2.getPositions().length : 0
        );
        if (maxRows == 0) maxRows = 6;

        for (int i = 0; i < maxRows; i++) {
            String cell1 = renderCellContent(p1.getPositions(), i);
            String cell2 = renderCellContent(p2.getPositions(), i);

            sb.append(GRID_MARGIN)
                    .append("│ ").append(centerText(cell1, 9)).append(" │")
                    .append(GRID_GAP)
                    .append("│ ").append(centerText(cell2, 9)).append(" │\n");

            if (i < maxRows - 1) {
                sb.append(GRID_MARGIN).append("├───────────┤").append(GRID_GAP).append("├───────────┤\n");
            }
        }
        sb.append(GRID_MARGIN).append("└───────────┘").append(GRID_GAP).append("└───────────┘\n\n");

        // --- FOOTER ---
        sb.append("╔").append(H_BORDER).append("╗\n");

        String stats = String.format(" %s : %d pts                  %s : %d pts ",
                truncate(p1.getUsername(), 10), p1.getPoints(),
                truncate(p2.getUsername(), 10), p2.getPoints());

        sb.append(centerLine(stats)).append("\n");
        sb.append("╚").append(H_BORDER).append("╝\n");

        // --- LEGEND ---
        String row1 = String.format("   %-14s%-14s%-14s%-14s",
                "[N] Novice", "[F] Fighter", "[W] Wizard", "[H] Healer");

        String row2 = String.format("   %-14s%-14s",
                "[X] Captured", "[.] Empty");

        sb.append(row1).append("\n");
        sb.append(row2).append("\n");

        return sb.toString();
    }

    private static String getInfo(GameState gameState, String safePlayer, String safeRoom) {
        String turnLabel;
        if (gameState.isActionPending()) {
            // UPDATED: Include position of the pending action
            Integer pos = gameState.getActionPendingOn();
            String posStr = (pos != null) ? String.valueOf(pos) : "?";
            turnLabel = String.format("Action [%s]: %s", posStr, safePlayer);
        } else {
            turnLabel = "Picker: " + safePlayer;
        }

        return String.format("Room: %s  Mode: %s  Status: %s  %s",
                safeRoom,
                gameState.getGameMode(),
                gameState.getStatus(),
                turnLabel);
    }

    // --- HELPER METHODS ---

    private static String renderCellContent(Position[] positions, int index) {
        if (positions == null || index >= positions.length || positions[index] == null) {
            return ".";
        }
        Position p = positions[index];
        if (p.isCapturedByOpponent()) {
            return "X";
        }
        Actor actor = p.getActor();
        if (actor == null) {
            return ".";
        }
        return getSymbol(actor.getCurrentState());
    }

    private static String getSymbol(Specialization s) {
        if (s == null) return "?";
        return switch (s) {
            case NOVICE -> "N";
            case FIGHTER -> "F";
            case WIZARD -> "W";
            case HEALER -> "H";
            default -> "?";
        };
    }

    private static String centerLine(String text) {
        int contentWidth = GameRendererService.WIDTH - 2;
        if (text.length() > contentWidth) {
            text = text.substring(0, contentWidth - 3) + "...";
        }
        int padding = (contentWidth - text.length()) / 2;
        String left = " ".repeat(padding);
        String right = " ".repeat(contentWidth - text.length() - padding);
        return "║" + left + text + right + "║";
    }

    private static String centerText(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        String left = " ".repeat(padding);
        String right = " ".repeat(width - text.length() - padding);
        return left + text + right;
    }

    private static String truncate(String s, int maxLength) {
        if (s == null) return "-";
        if (s.length() <= maxLength) return s;
        return s.substring(0, maxLength - 2) + "..";
    }
}
