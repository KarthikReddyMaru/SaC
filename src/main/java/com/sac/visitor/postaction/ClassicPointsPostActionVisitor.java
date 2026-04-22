package com.sac.visitor.postaction;

import com.sac.model.GameState;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ActionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.service.PointsService;
import com.sac.strategy.action.Evolve;
import com.sac.strategy.action.Kamikaze;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ClassicPointsPostActionVisitor implements PostActionVisitor {

    private final PointsService pointsService;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    public void visit(Kamikaze kamikaze, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);
        String opponent = gameState.getOpponent(username).getUsername();

        pointsService.addPoints(roomId, username, kamikaze.pointsForSuccessfulAction());
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        gameState.setCurrentPlayerId(opponent);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }

    @Override
    public void visit(Evolve evolve, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);

        Specialization requestedTransition = actionContext.getSpecialization();

        GameState gameState = gameStateService.getGameState(roomId);
        int actionPerformingOn = gameState.getActionPendingOn();
        String opponent = gameState.getOpponent(username).getUsername();

        messageService.broadcastMessage(
                MessageFormat.evolveSuccessAction(username, actionPerformingOn, Specialization.NOVICE, requestedTransition),
                roomId);

        pointsService.addPoints(roomId, username, evolve.pointsForSuccessfulAction());
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        gameState.setCurrentPlayerId(opponent);
        messageService.broadcastMessage(MessageFormat.chooseMessage(username), roomId);
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
    }
}