package com.sac.strategy.mode;

import com.sac.factory.ActionHandlerRegistry;
import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Action;
import com.sac.util.MessageFormat;
import com.sac.visitor.postaction.ClassicPointsPostActionVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassicPoints implements Mode {

    private final int pointsToReach = 21;

    private final ClassicPointsPostActionVisitor classicPointsPostActionVisitor;
    private final ActionHandlerRegistry actionHandlerRegistry;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public String computeWinner(String roomId) {
        GameState gameState = gameStateService.getGameState(roomId);
        List<GameState.Player> players = gameState.getPlayers();
        GameState.Player winner = players.stream()
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
        return GameMode.CLASSIC_POINTS;
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) throws IOException {
        Action action = actionHandlerRegistry.getInstance(actionContext.getGameAction());
        action.performAction(webSocketSession, actionContext, roomId);
        action.postAction(classicPointsPostActionVisitor, webSocketSession);
        String winner = this.computeWinner(roomId);
        if (winner != null) {
            log.info("Game completed, preparing to close connections of room - {}", roomId);
            messageService.broadcastMessage(MessageFormat.endGameWithWinner(winner, gameStateService.getGameState(roomId)), roomId);
            webSocketSession.close(CloseStatus.NORMAL);
        }
    }
}
