package com.sac.visitor.postaction;

import com.sac.model.GameState;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.service.PointsService;
import com.sac.strategy.action.AttackAndCapture;
import com.sac.strategy.action.Evolve;
import com.sac.strategy.action.Kamikaze;
import com.sac.strategy.action.Spawn;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import com.sac.util.mode.ClassicPointsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ClassicPointsPostActionVisitor implements PostActionVisitor {

    private final PointsService pointsService;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public void visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        String opponent = gameState.getOpponent(username).getUsername();

        ClassicPointsUtil.transitionRollToNextPlayer(opponent, gameState);
        pointsService.addPoints(roomId, username, spawn.pointsForSuccessfulAction());
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @Override
    public void visit(Kamikaze kamikaze, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        String opponent = gameState.getOpponent(username).getUsername();

        ClassicPointsUtil.transitionRollToNextPlayer(opponent, gameState);
        pointsService.addPoints(roomId, username, kamikaze.pointsForSuccessfulAction());
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @Override
    public void visit(Evolve evolve, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        String opponent = gameState.getOpponent(username).getUsername();

        ClassicPointsUtil.transitionRollToNextPlayer(opponent, gameState);
        pointsService.addPoints(roomId, username, evolve.pointsForSuccessfulAction());
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @Override
    public void visit(AttackAndCapture attackAndCapture, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        pointsService.addPoints(roomId, username, attackAndCapture.pointsForSuccessfulAction());
        ClassicPointsUtil.transitionRollToNextPlayer(username, gameState);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

}