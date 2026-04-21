package com.sac.visitor.prechoose;

import com.sac.model.GameState;
import com.sac.model.message.PositionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.position.Roll;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ClassicPointsPreChooseVisitor implements PreChooseVisitor {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public boolean visit(Roll roll, WebSocketSession webSocketSession, PositionContext positionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);
        int rolledNumber = positionContext.getPosition();

        if (!gameState.getCurrentPlayerId().equals(username)) {
            String message = String.format("Wait for your turn, %s's turn now", gameState.getOpponent(username).getUsername());
            messageService.sendToSender(webSocketSession, message, ServerResponse.Type.ERROR);
            return false;
        }
        else if (gameState.isActionPending()) {
            String errorMsg = String.format("%s needs to perform action before choosing",
                                            gameState.getCurrentPlayerId());
            messageService.sendToSender(webSocketSession,
                                        MessageFormat.systemError(errorMsg),
                                        ServerResponse.Type.ERROR);
            return false;
        }
        else if (rolledNumber < 1 || rolledNumber > 10) {
            String errorMsg = "Only positions from 1 to 10 are allowed";
            messageService.sendToSender(webSocketSession,
                                        MessageFormat.systemError(errorMsg),
                                        ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }

}
