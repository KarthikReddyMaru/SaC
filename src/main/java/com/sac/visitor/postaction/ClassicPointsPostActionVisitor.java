package com.sac.visitor.postaction;

import com.sac.model.GameState;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.service.PointsService;
import com.sac.strategy.action.*;
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
    public void visit(Drop drop, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        String opponent = gameState.getOpponent(username).getUsername();

        ClassicPointsUtil.transitionRollToNextPlayer(opponent, gameState);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

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
    public void visit(Revert revert, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        String opponent = gameState.getOpponent(username).getUsername();

        ClassicPointsUtil.transitionRollToNextPlayer(opponent, gameState);
        pointsService.addPoints(roomId, username, revert.pointsForSuccessfulAction());
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @Override
    public void visit(Promote promote, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        String opponent = gameState.getOpponent(username).getUsername();

        ClassicPointsUtil.transitionRollToNextPlayer(opponent, gameState);
        pointsService.addPoints(roomId, username, promote.pointsForSuccessfulAction());
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @Override
    public void visit(Capture capture, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        pointsService.addPoints(roomId, username, capture.pointsForSuccessfulAction());
        ClassicPointsUtil.transitionRollToNextPlayer(username, gameState);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @Override
    public void visit(BlackOut blackOut, WebSocketSession webSocketSession, ActionContext actionContext) {
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        String opponent = gameState.getOpponent(username).getUsername();

        pointsService.addPoints(roomId, username, Integer.parseInt(actionContext.getAdditionalInfo()));
        ClassicPointsUtil.transitionRollToNextPlayer(opponent, gameState);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }
}