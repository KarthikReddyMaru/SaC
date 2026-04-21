package com.sac.visitor.preaction;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Kamikaze;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ClassicPointsPreAction implements PreActionVisitor {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public void visit(Kamikaze kamikaze, WebSocketSession webSocketSession, ActionContext context) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);
        Integer opponentPositionId = context.getDestinationPosition();

        Position position = gameState.getPlayerPosition(username, gameState.getActionPendingOn());
        Actor actor = position.getActor();

        if (!gameState.isActionPending() ||
            !gameState.getCurrentPlayerId().equals(username) ||
            gameState.getActionPendingOn() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.illegalAction());
        } else if (opponentPositionId == null && context.getSourcePosition() == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noDestinationProvided());
        } else if (actor == null) {
            messageService.sendToSender(webSocketSession, MessageFormat.noActorPresent(gameState.getActionPendingOn()));
        } else if (!actor.getAllowedActions().contains(context.getGameAction())) {
            messageService.sendToSender(webSocketSession, MessageFormat.actorCannotPerform(
                    actor.getCurrentState(), context.getGameAction()));
        }
    }

}
