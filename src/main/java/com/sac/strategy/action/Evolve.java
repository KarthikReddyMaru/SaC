package com.sac.strategy.action;

import com.sac.factory.ActorFactory;
import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ActionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.service.PointsService;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.sac.strategy.action.GameAction.EVOLVE;

@Component
@RequiredArgsConstructor
public class Evolve implements Action {

    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final PointsService pointsService;

    @Override
    public GameAction getActionType() {
        return EVOLVE;
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        Specialization requestedTransition = actionContext.getSpecialization();
        GameState gameState = gameStateService.getGameState(roomId);
        Position position = gameStateService.getPlayerPosition(roomId, username, gameState.getActionPendingOn());
        int actionPerformingOn = gameState.getActionPendingOn();
        Specialization currentSpecialization = position.getActor()
                                                       .getCurrentState();
        position.setActor(ActorFactory.getInstance(requestedTransition));
        postProcessAction(roomId, username, actionPerformingOn, currentSpecialization,
                          requestedTransition, gameState);
    }

    private void postProcessAction(String roomId, String username, int actionPerformingOn,
                                   Specialization from, Specialization requestedTransition, GameState gameState) {

    }


    @Override
    public int pointsForSuccessfulAction() {
        return 0;
    }
}
