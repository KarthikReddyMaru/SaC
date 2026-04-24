package com.sac.strategy.mode;

import com.sac.factory.ActionHandlerRegistry;
import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.message.ActionContext;
import com.sac.model.message.PositionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Action;
import com.sac.strategy.position.Roll;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import com.sac.visitor.postaction.ClassicPointsPostActionVisitor;
import com.sac.visitor.preaction.ClassicPointsPreActionVisitor;
import com.sac.visitor.prechoose.ClassicPointsPreChooseVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

import static com.sac.model.GameState.GameplayStatus.FINISHED;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassicPoints implements Mode {

    @Value("${classic.points}")
    private int pointsToReach;

    private final ActionHandlerRegistry actionHandlerRegistry;
    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final Roll roll;

    private final ClassicPointsPreChooseVisitor classicPointsPreChooseVisitor;
    private final ClassicPointsPostActionVisitor classicPointsPostActionVisitor;
    private final ClassicPointsPreActionVisitor classicPointsPreActionVisitor;

    @Override
    public String computeWinner(String roomId) {
        GameState gameState = gameStateService.getGameState(roomId);
        List<GameState.Player> players = gameState.getPlayers();
        GameState.Player winner = players.stream()
                                         .filter(player -> player.getPoints() >= pointsToReach)
                                         .findFirst().orElse(null);
        if (winner != null) {
            gameState.setGameplayStatus(FINISHED);
            return winner.getUsername();
        }
        return null;
    }

    @Override
    public GameMode getMode() {
        return GameMode.CLASSIC_POINTS;
    }

    @Override
    public void performChoose(WebSocketSession webSocketSession, PositionContext message) throws IOException {
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        if (roll.preChoose(classicPointsPreChooseVisitor, webSocketSession, message)) {
            roll.handle(webSocketSession, message, roomId);
        }
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) throws IOException {
        Action action = actionHandlerRegistry.getInstance(actionContext.getGameAction());
        if (action.preAction(classicPointsPreActionVisitor, webSocketSession, actionContext)) {
            action.performAction(webSocketSession, actionContext, roomId);
            action.postAction(classicPointsPostActionVisitor, webSocketSession, actionContext);
            String winner = this.computeWinner(roomId);
            if (winner != null) {
                GameState gameState = gameStateService.getGameState(roomId);
                log.info("Game completed, preparing to close connections of room - {}", roomId);
                gameState.setGameplayStatus(FINISHED);
                gameState.setWinner(winner);
                gameState.setActionPendingOn(null);
                gameState.setCurrentPlayerId(null);
                gameState.setActionPending(false);
                messageService.broadcastMessage(
                        MessageFormat.endGameWithWinner(gameState), roomId);
                webSocketSession.close(CloseStatus.NORMAL);
            }
        }
    }
}
