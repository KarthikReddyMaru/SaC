package com.sac.visitor.postaction;

import com.sac.model.GameMode;
import com.sac.model.GameState;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.service.PointsService;
import com.sac.strategy.action.*;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import com.sac.util.mode.ClassicPointsUtil;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.sac.model.GameMode.CLASSIC_POINTS;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("LoggingSimilarMessage")
public class ClassicPointsPostActionVisitor implements PostActionVisitor {

    private final PointsService pointsService;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public GameMode getMode() {
        return CLASSIC_POINTS;
    }

    @Override @WithSpan("postaction.dtop")
    public void visit(Drop drop, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        ClassicPointsUtil.transitionRollToNextPlayer(gameState);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        log.info("Transitioned roll to {}", gameStateService.getUsernameFromId(gameState.getCurrentPlayerId(), roomId));
    }

    @Override
    public void visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        ClassicPointsUtil.transitionRollToNextPlayer(gameState);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        log.info("Transitioned roll to {}", gameStateService.getUsernameFromId(gameState.getCurrentPlayerId(), roomId));
    }

    @Override
    public void visit(Revert revert, WebSocketSession webSocketSession, ActionContext actionContext) {

        String playerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        ClassicPointsUtil.transitionRollToNextPlayer(gameState);
        pointsService.addPoints(roomId, playerId, revert.pointsForSuccessfulAction());
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        log.info("Transitioned roll to {}", gameStateService.getUsernameFromId(gameState.getCurrentPlayerId(), roomId));
    }

    @Override
    public void visit(Promote promote, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        ClassicPointsUtil.transitionRollToNextPlayer(gameState);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        log.info("Transitioned roll to {}", gameStateService.getUsernameFromId(gameState.getCurrentPlayerId(), roomId));
    }

    @Override
    public void visit(Capture capture, WebSocketSession webSocketSession, ActionContext actionContext) {

        String playerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        pointsService.addPoints(roomId, playerId, capture.pointsForSuccessfulAction());
        ClassicPointsUtil.transitionRollToCurrentPlayer(gameState);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        log.info("Transitioned roll to {}", gameStateService.getUsernameFromId(gameState.getCurrentPlayerId(), roomId));
    }

    @Override
    public void visit(BlackOut blackOut, WebSocketSession webSocketSession, ActionContext actionContext) {
        String playerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        pointsService.addPoints(roomId, playerId, Integer.parseInt(actionContext.getAdditionalInfo()));
        ClassicPointsUtil.transitionRollToNextPlayer(gameState);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        log.info("Transitioned roll to {}", gameStateService.getUsernameFromId(gameState.getCurrentPlayerId(), roomId));
    }
}