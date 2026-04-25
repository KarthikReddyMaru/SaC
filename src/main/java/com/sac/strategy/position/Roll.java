package com.sac.strategy.position;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.message.PositionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Spawn;
import com.sac.util.MessageFormat;
import com.sac.visitor.prechoose.PreChooseVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.sac.model.GameState.State.ACTION_REQUIRED;
import static com.sac.model.GameState.State.ROLL;

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
        String opponentPlayer = gameState.getOpponent(currentPlayer)
                                         .getUsername();
        int positionId = message.getPosition();
        Position position = gameState.getPlayerPosition(currentPlayer, positionId);

        if (position.isCapturedByOpponent()) {
            messageService.broadcastMessage(MessageFormat.capturedTrouble(opponentPlayer, positionId), roomId);
            gameState.setActionPending(false);
            gameState.setActionPendingOn(null);
            gameState.setCurrentPlayerId(opponentPlayer);
            gameState.setActionPendingOnActor(null);
            gameState.setState(ROLL);
            messageService.broadcastMessage(MessageFormat.rollMessage(opponentPlayer), roomId);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        } else if (position.getActor() == null) {
            gameState.setActionPending(true);
            gameState.setActionPendingOn(positionId);
            gameState.setActionPendingOnActor(null);
            spawn.performAction(webSocketSession, null, roomId);
            gameState.setActionPending(false);
            gameState.setActionPendingOn(null);
            gameState.setActionPendingOnActor(null);
            gameState.setState(ROLL);
            gameState.setCurrentPlayerId(opponentPlayer);
            messageService.broadcastMessage(MessageFormat.spawnSuccessAction(currentPlayer, positionId), roomId);
            messageService.broadcastMessage(MessageFormat.rollMessage(opponentPlayer), roomId);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        } else {
            gameState.setCurrentPlayerId(currentPlayer);
            gameState.setActionPending(true);
            gameState.setActionPendingOn(positionId);
            gameState.setState(ACTION_REQUIRED);
            gameState.setActionPendingOnActor(position.getActor()
                                                      .getCurrentState());
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
