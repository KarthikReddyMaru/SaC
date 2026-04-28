package com.sac.visitor.prechoose;

import com.sac.model.GameState;
import com.sac.model.message.PositionContext;
import com.sac.model.message.PositionSelectionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.position.Roll;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Random;

import static com.sac.model.GameState.GameplayStatus.*;
import static com.sac.model.GameState.State.ROLL;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassicPointsPreChooseVisitor implements PreChooseVisitor {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    private final Random random = new Random();

    @Value("${player.total_positions}")
    private int maxPositionsPerPlayer;

    @Override
    public boolean visit(Roll roll, WebSocketSession webSocketSession, PositionContext positionContext) {

        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);

        if (gameState.getGameplayStatus().equals(INIT)) {
            logAndSendErrorMessage(webSocketSession, "Game not initialized", "game_uninitialized");
            return false;
        } else if (gameState.getGameplayStatus().equals(OFFLINE)) {
            logAndSendErrorMessage(webSocketSession, "Wait till opponent returns", "game_offline");
            return false;
        }

        String actualPlayerId = gameState.getCurrentPlayerId();
        if (!gameState.getCurrentPlayerId().equals(clientId)) {
            String message = String.format("Wait for your turn, %s's turn now",
                                           gameStateService.getUsernameFromId(actualPlayerId, roomId));
            logAndSendErrorMessage(webSocketSession, message, "wrong_turn");
            return false;
        } else if (!gameState.getState().equals(ROLL) || gameState.isActionPending()) {
            String msg = String.format("%s needs to perform action before choosing",
                                            gameStateService.getUsernameFromId(actualPlayerId, roomId));
            logAndSendErrorMessage(webSocketSession, msg, "action_pending");
            return false;
        }

        if (!gameState.isWildCardActive()) {

            int targetOutcome = random.nextInt(1, 12);

            int i1;
            int i2;
            boolean isWildcard = false;

            if (targetOutcome == 11) {

                isWildcard = true;
                i1 = 0;
                i2 = 0;
                gameState.setWildCardActive(true);

            } else {

                int minI1 = Math.max(0, targetOutcome - 5);
                int maxI1 = Math.min(5, targetOutcome);

                i1 = random.nextInt(minI1, maxI1 + 1);
                i2 = targetOutcome - i1;
                positionContext.setPosition(targetOutcome);
            }

            PositionSelectionContext positionSelectionContext = new PositionSelectionContext(new int[]{i1, i2}, isWildcard);
            messageService.broadcastMessage(MessageFormat.rollResult(positionSelectionContext), roomId);

            if (isWildcard) {
                Span.current().addEvent("Wildcard activated");
                log.info("Wildcard activated.");
                return false;
            }
        }

        int rolledNumber = positionContext.getPosition();
        gameState.setWildCardActive(false);

        if (rolledNumber < 1 || rolledNumber > maxPositionsPerPlayer) {
            String errorMsg = "Only positions from 1 to " + maxPositionsPerPlayer + " are allowed";
            gameState.setState(ROLL);
            logAndSendErrorMessage(webSocketSession, errorMsg, "invalid_roll");
            return false;
        }

        Span.current().addEvent("Roll").setAttribute("roll.value", positionContext.getPosition());
        log.info("ROLL_RESULT: {}", positionContext.getPosition());
        return true;
    }

    private void logAndSendErrorMessage(WebSocketSession webSocketSession, String message, String trace_reason) {
        log.warn("Roll rejected: {}", message);
        Span.current().addEvent(trace_reason).setAttribute("reason", message);
        messageService.sendRawPayload(webSocketSession, MessageFormat.systemError(message));
    }

}
