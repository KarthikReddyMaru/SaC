package com.sac.visitor.preaction;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ActionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Evolve;
import com.sac.strategy.action.Kamikaze;
import com.sac.strategy.action.Spawn;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ClassicPointsPreActionVisitor implements PreActionVisitor {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public boolean visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);

        if (gameState.getActionPendingOn() == null ||
            !gameState.isActionPending() ||
            !gameState.getCurrentPlayerId().equals(username)) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (gameStateService.getPlayerPosition(roomId, username, gameState.getActionPendingOn()).getActor() != null) {
            String errorMsg = "An actor already present in this position, choose different action";
            messageService.sendToSender(webSocketSession, errorMsg, ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(Kamikaze kamikaze, WebSocketSession webSocketSession, ActionContext context) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);
        Integer opponentPositionId = context.getDestinationPosition();

        if (!gameState.isActionPending() ||
            !gameState.getCurrentPlayerId().equals(username) ||
            gameState.getActionPendingOn() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
            return false;
        } else if (opponentPositionId == null && context.getSourcePosition() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noDestinationProvided());
            return false;
        }

        Position position = gameState.getPlayerPosition(username, gameState.getActionPendingOn());
        Actor actor = position.getActor();

        if (actor == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noActorPresent(gameState.getActionPendingOn()));
            return false;
        } else if (!actor.getAllowedActions().contains(context.getGameAction())) {
            messageService.sendToSender(webSocketSession, MessageFormat.actorCannotPerform(
                    actor.getCurrentState(), context.getGameAction()));
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(Evolve evolve, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        Specialization requestedTransition = actionContext.getSpecialization();
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
}
