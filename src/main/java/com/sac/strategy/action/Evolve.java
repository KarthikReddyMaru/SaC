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

    @Override
    public GameAction getActionType() {
        return EVOLVE;
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        Specialization requestedTransition = actionContext.getSpecialization();
        if (preProcessChecks(webSocketSession, username, roomId, requestedTransition)) {
            GameState gameState = gameStateService.getGameState(roomId);
            Position position = gameStateService.getPlayerPosition(roomId, username, gameState.getActionPendingOn());
            int actionPerformingOn = gameState.getActionPendingOn();
            position.setActor(ActorFactory.getInstance(requestedTransition));
            gameStateService.getPlayer(roomId, username).addPoints(pointsForSuccessfulAction());
            postProcessAction(roomId, username, actionPerformingOn, position.getActor().getCurrentState(),
                    requestedTransition, gameState);
        }
    }

    private boolean preProcessChecks(WebSocketSession webSocketSession, String username,
                                    String roomId, Specialization requestedTransition) {

        GameState gameState = gameStateService.getGameState(roomId);
        if (gameState.getActionPendingOn() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (!gameState.isActionPending() || !gameState.getCurrentPlayerId().equals(username)) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        }

        Position position = gameStateService.getPlayerPosition(roomId, username, gameState.getActionPendingOn());
        Actor actor = position.getActor();
        if (actor == null) {
            messageService.sendToSender(webSocketSession, "SPAWN actor before EVOLVE", ServerResponse.Type.ERROR);
            return false;
        } else if (requestedTransition == null) {
            messageService.sendToSender(webSocketSession, "Choose Specialization to evolve", ServerResponse.Type.ERROR);
            return false;
        } else if (!actor.getAllowedTransitions().contains(requestedTransition) || actor.getCurrentState().equals(requestedTransition)) {
            String errorMessage = String.format("%s cannot EVOLVE to %s",
                    actor.getCurrentState(), requestedTransition);
            messageService.sendToSender(webSocketSession, errorMessage, ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }

    private void postProcessAction(String roomId, String username, int actionPerformingOn,
                                   Specialization from, Specialization requestedTransition, GameState gameState) {
        messageService.broadcastMessage(
                MessageFormat.evolveSuccessAction(username, actionPerformingOn, from, requestedTransition),
                roomId);
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        messageService.broadcastMessage(MessageFormat.chooseMessage(username), roomId);
    }


    @Override
    public int pointsForSuccessfulAction() {
        return 1;
    }
}
