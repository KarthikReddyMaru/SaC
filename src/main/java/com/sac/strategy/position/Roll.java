package com.sac.strategy.position;

import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.model.message.PositionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Spawn;
import com.sac.util.MessageFormat;
import com.sac.util.mode.ClassicPointsUtil;
import com.sac.visitor.prechoose.PreChooseVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Roll implements PositionSelectionHandlerStrategy {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    private final Spawn spawn;

    @Override
    public void handle(WebSocketSession webSocketSession, PositionContext message, String roomId) throws IOException {

        GameState gameState = gameStateService.getGameState(roomId);
        String currentPlayer = gameState.getCurrentPlayerId();
        int positionId = message.getPosition();
        Position position = gameState.getPlayerPosition(currentPlayer, positionId);

        if (position.isCapturedByOpponent()) {
            String capturedPlayerId = position.getBelongsTo();
            String capturedPlayerUsername = gameStateService.getUsernameFromId(capturedPlayerId, roomId);
            messageService.broadcastMessage(MessageFormat.capturedTrouble(capturedPlayerUsername, positionId), roomId);
            ClassicPointsUtil.transitionRollToNextPlayer(gameState);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        } else if (position.getActor() == null) {
            ClassicPointsUtil.requirePlayerAction(currentPlayer, gameState, positionId);
            spawn.performAction(webSocketSession, null, roomId);
            ClassicPointsUtil.transitionRollToNextPlayer(gameState);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        } else {
            ClassicPointsUtil.requirePlayerAction(currentPlayer, gameState, positionId);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        }
    }

    @Override
    public PositionSelection getPositionSelectionType() {
        return PositionSelection.ROLL;
    }

    @Override
    public boolean preChoose(PreChooseVisitor preChooseVisitor, WebSocketSession webSocketSession,
                             PositionContext message) {
        return preChooseVisitor.visit(this, webSocketSession, message);
    }
}
