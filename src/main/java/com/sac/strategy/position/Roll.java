package com.sac.strategy.position;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.message.PositionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Spawn;
import com.sac.util.MessageFormat;
import com.sac.util.mode.ClassicPointsUtil;
import com.sac.visitor.prechoose.PreChooseVisitor;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("LoggingSimilarMessage")
public class Roll implements PositionSelectionHandlerStrategy {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    private final Spawn spawn;

    @Override @WithSpan("roll.handle")
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

            log.info("Player landed on captured territory at {}", message.getPosition());
            log.info("Transitioned roll to {}", gameStateService.getUsernameFromId(gameState.getCurrentPlayerId(), roomId));
            Span.current().addEvent("captured_territory").setAttribute("position", message.getPosition());

        } else if (position.getActor() == null) {
            ClassicPointsUtil.requirePlayerAction(currentPlayer, gameState, positionId);
            spawn.performAction(webSocketSession, null, roomId);
            ClassicPointsUtil.transitionRollToNextPlayer(gameState);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
            log.info("Transitioned roll to {}", gameStateService.getUsernameFromId(gameState.getCurrentPlayerId(), roomId));
            Span.current().addEvent("empty_territory").setAttribute("position", message.getPosition());


        } else {
            ClassicPointsUtil.requirePlayerAction(currentPlayer, gameState, positionId);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);

            log.info("Player landed on owned territory, ACTION_REQUIRED at {}", message.getPosition());
            Span.current().addEvent("empty_territory").setAttribute("position", message.getPosition());

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
