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
        GameState gameState = gameStateService.getGameState(roomId);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        if (gameState.getActionPendingOn() == -1) { messageService.sendToSender(webSocketSession, MessageFormat.illegalAction()); return; };
        Position position = gameStateService.getPlayerPosition(roomId, username, gameState.getActionPendingOn());
        Specialization requestedTransition = actionContext.getSpecialization();
        if (preProcessChecks(webSocketSession, username, gameState, position, requestedTransition)) {
            int actionPerformingOn = gameState.getActionPendingOn();
            Specialization from = position.getActor().getCurrentState();
            position.setActor(ActorFactory.getInstance(requestedTransition));
            gameStateService.getPlayer(roomId, username).addPoints(pointsForSuccessfulAction());
            messageService.broadcastMessage(
                    MessageFormat.evolveSuccessAction(username, actionPerformingOn, from, requestedTransition),
                    roomId);
            gameState.setActionPending(false);
            gameState.setActionPendingOn(-1);
            messageService.broadcastMessage(MessageFormat.chooseMessage(username), roomId);
        }
    }

    public boolean preProcessChecks(WebSocketSession webSocketSession, String username,
                                    GameState gameState, Position position, Specialization requestedTransition) {
        if (!gameState.isActionPending() || !gameState.getCurrentPlayerId().equals(username)) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        }
        Actor actor = position.getActor();
        if (actor == null) {
            messageService.sendToSender(webSocketSession, "SPAWN actor before EVOLVE", ServerResponse.Type.ERROR);
            return false;
        }
        else if (actor.isFrozen()) {
            messageService.sendToSender(webSocketSession, "UNFREEZE actor before EVOLVE", ServerResponse.Type.ERROR);
            return false;
        }
        else if (requestedTransition == null) {
            messageService.sendToSender(webSocketSession, "Choose Specialization to evolve", ServerResponse.Type.ERROR);
            return false;
        }
        else if (!actor.getAllowedTransitions().contains(requestedTransition)) {
            String errorMessage = String.format("%s cannot EVOLVE to %s",
                    actor.getCurrentState(), requestedTransition);
            messageService.sendToSender(webSocketSession, errorMessage, ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 1;
    }
}
