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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Random;

import static com.sac.model.GameState.State.ROLL;

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

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);

        if (!gameState.getCurrentPlayerId()
                      .equals(username)) {
            String message = String.format("Wait for your turn, %s's turn now", gameState.getOpponent(username)
                                                                                         .getUsername());
            messageService.sendSystemMessage(webSocketSession, message, ServerResponse.Type.ERROR);
            return false;
        } else if (!gameState.getState()
                             .equals(ROLL) || gameState.isActionPending()) {
            String errorMsg = String.format("%s needs to perform action before choosing",
                                            gameState.getCurrentPlayerId());
            messageService.sendSystemMessage(webSocketSession, errorMsg, ServerResponse.Type.ERROR);

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
                return false;
            }
        }

        int rolledNumber = positionContext.getPosition();
        gameState.setWildCardActive(false);

        if (rolledNumber < 1 || rolledNumber > maxPositionsPerPlayer) {
            String errorMsg = "Only positions from 1 to " + maxPositionsPerPlayer + " are allowed";
            messageService.sendSystemMessage(webSocketSession, errorMsg, ServerResponse.Type.ERROR);
            gameState.setState(ROLL);
            return false;
        }
        return true;
    }

}
