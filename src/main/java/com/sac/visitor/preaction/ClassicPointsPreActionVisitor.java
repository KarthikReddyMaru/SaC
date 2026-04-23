package com.sac.visitor.preaction;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ActionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.AttackAndCapture;
import com.sac.strategy.action.Evolve;
import com.sac.strategy.action.Kamikaze;
import com.sac.strategy.action.Spawn;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ClassicPointsPreActionVisitor implements PreActionVisitor {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Value("${player.total_positions}")
    private int maxPositionPerPlayer;

    @Override
    public boolean visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);

        if (baseCheck(webSocketSession, username, gameState)) {
            return false;
        } else {
            Actor actor = gameStateService.getPlayerPosition(roomId, username, gameState.getActionPendingOn())
                                          .getActor();
            if (actor != null) {
                String errorMsg = "An actor already present in this position, choose different action";
                messageService.sendSystemMessage(webSocketSession, errorMsg, ServerResponse.Type.ERROR);
                messageService.sendRawPayload(webSocketSession,
                                              MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                                  .name()));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean visit(Kamikaze kamikaze, WebSocketSession webSocketSession, ActionContext context) {

        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);

        if (baseCheck(webSocketSession, username, gameState)) {
            return false;
        }

        Position position = gameState.getPlayerPosition(username, gameState.getActionPendingOn());
        Actor actor = position.getActor();

        if (context.getDestinationPosition() == null && context.getSourcePosition() == null) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                              .name()));
            return false;
        }

        if ((context.getSourcePosition() != null &&
             (context.getSourcePosition() < 1 || context.getSourcePosition() > maxPositionPerPlayer)) ||
            (context.getDestinationPosition() != null &&
             (context.getDestinationPosition() < 1 || context.getDestinationPosition() > maxPositionPerPlayer))) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                              .name()));

        }

        if (actor == null) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.noActorPresent(gameState.getActionPendingOn()));
            return false;
        } else if (!actor.getAllowedActions()
                         .contains(context.getGameAction())) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.actorCannotPerform(actor.getCurrentState(),
                                                                                             context.getGameAction()));
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                              .name()));
            return false;
        } else if (context.getSourcePosition() != null && gameState.getActionPendingOn()
                                                                   .equals(context.getSourcePosition())) {
            messageService.sendSystemMessage(webSocketSession, "You cannot perform Kamikaze on the same position");
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                              .name()));
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

        if (baseCheck(webSocketSession, username, gameState)) return false;

        Position position = gameStateService.getPlayerPosition(roomId, username, gameState.getActionPendingOn());
        Actor actor = position.getActor();

        if (actor == null) {
            messageService.sendSystemMessage(webSocketSession, "SPAWN actor before EVOLVE", ServerResponse.Type.ERROR);
            return false;
        } else if (requestedTransition == null) {
            messageService.sendSystemMessage(webSocketSession, "Choose Specialization to evolve",
                                             ServerResponse.Type.ERROR);
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                              .name()));
            return false;
        } else if (!actor.getAllowedTransitions()
                         .contains(requestedTransition) || actor.getCurrentState()
                                                                .equals(requestedTransition)) {
            String errorMessage = String.format("%s cannot EVOLVE to %s", actor.getCurrentState(), requestedTransition);
            messageService.sendSystemMessage(webSocketSession, errorMessage, ServerResponse.Type.ERROR);
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                              .name()));
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(AttackAndCapture attackAndCapture, WebSocketSession webSocketSession,
                         ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);
        Integer playerPositionId = gameState.getActionPendingOn();

        if (baseCheck(webSocketSession, username, gameState)) return false;

        Position playerPosition = gameState.getPlayerPosition(username, playerPositionId);
        Actor actor = playerPosition.getActor();
        Integer opponentPositionId = actionContext.getDestinationPosition();

        if (actor == null) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.noActorPresent(playerPosition.getPositionId()));
            return false;
        } else if (!actor.getAllowedActions()
                         .contains(attackAndCapture.getActionType())) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.actorCannotPerform(actor.getCurrentState(),
                                                                           attackAndCapture.getActionType()));
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                              .name()));
            return false;
        } else if (opponentPositionId == null ||
                   (actionContext.getDestinationPosition() < 1 ||
                    actionContext.getDestinationPosition() > maxPositionPerPlayer)) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                              .name()));
            return false;
        } else if (gameState.getOpponentPosition(username, opponentPositionId)
                            .isCapturedByOpponent()) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.capturedTrouble(username, opponentPositionId));
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.retryActionAgain(actor.getCurrentState()
                                                                              .name()));
            return false;
        }
        return true;
    }

    private boolean baseCheck(WebSocketSession webSocketSession, String username, GameState gameState) {
        if (gameState.getActionPendingOn() == null ||
            !gameState.isActionPending() ||
            !gameState.getCurrentPlayerId()
                      .equals(username)) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.illegalAction());
            if (username.equals(gameState.getCurrentPlayerId()))
                messageService.sendRawPayload(webSocketSession, MessageFormat.rollAction());
            return true;
        }
        return false;
    }
}
