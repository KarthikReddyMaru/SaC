package com.sac.service;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import java.util.List;

public class GameRendererService {

    private static final int WIDTH = 62;
    private static final String H_BORDER = "═".repeat(WIDTH - 2);
    private static final String GAP = "                ";

    private GameRendererService() {} // Prevent instantiation

    public static String render(GameState gameState) {
        StringBuilder sb = new StringBuilder();

        // --- HEADER ---
        sb.append("╔").append(H_BORDER).append("╗\n");
        sb.append(centerLine("SaC ARENA", WIDTH)).append("\n");
        sb.append("╠").append(H_BORDER).append("╣\n");

        // Truncate variable fields to prevent layout breaking
        String safeRoom = truncate(gameState.getRoomId(), 12);

        // --- NEW LOGIC: Action vs Picker ---
        String turnLabel;
        String safePlayer = truncate(gameState.getCurrentPlayerId() != null ? gameState.getCurrentPlayerId() : "-", 10);

        if (gameState.isActionPending()) {
            turnLabel = "Action: " + safePlayer;
        } else {
            turnLabel = "Picker: " + safePlayer;
        }

        String info = String.format("Room: %s  Mode: %s  Status: %s  %s",
                safeRoom,
                gameState.getGameMode(),
                gameState.getStatus(),
                turnLabel);

        sb.append(centerLine(info, WIDTH)).append("\n");
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

        sb.append("        ").append(padRight(p1Header, 13))
                .append(GAP)
                .append(padRight(p2Header, 13)).append("\n");

        // Grid Top
        sb.append("      ┌───────────┐").append(GAP).append("┌───────────┐\n");

        int maxRows = Math.max(
                p1.getPositions() != null ? p1.getPositions().length : 0,
                p2.getPositions() != null ? p2.getPositions().length : 0
        );
        if (maxRows == 0) maxRows = 6;

        for (int i = 0; i < maxRows; i++) {
            String cell1 = renderCellContent(p1.getPositions(), i);
            String cell2 = renderCellContent(p2.getPositions(), i);

            sb.append("      │ ").append(centerText(cell1, 9)).append(" │")
                    .append(GAP)
                    .append("│ ").append(centerText(cell2, 9)).append(" │\n");

            if (i < maxRows - 1) {
                sb.append("      ├───────────┤").append(GAP).append("├───────────┤\n");
            }
        }
        sb.append("      └───────────┘").append(GAP).append("└───────────┘\n\n");

        // --- FOOTER ---
        sb.append("╔").append(H_BORDER).append("╗\n");
        String stats = String.format(" %s : %d pts                  %s : %d pts ",
                truncate(p1.getUsername(), 10), p1.getPoints(),
                truncate(p2.getUsername(), 10), p2.getPoints());

        sb.append(centerLine(stats, WIDTH)).append("\n");
        sb.append("╚").append(H_BORDER).append("╝\n");

        // --- LEGEND ---
        sb.append("   [N] Novice   [F] Fighter   [W] Wizard   [H] Healer\n");
        sb.append("   [+] Frozen   [X] Captured  [.] Empty\n");

        return sb.toString();
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

        String sym = getSymbol(actor.getCurrentState());
        return actor.isFrozen() ? "+" + sym : sym;
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

    private static String centerLine(String text, int totalWidth) {
        int contentWidth = totalWidth - 2;
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

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private static String truncate(String s, int maxLength) {
        if (s == null) return "-";
        if (s.length() <= maxLength) return s;
        return s.substring(0, maxLength - 2) + "..";
    }
}
