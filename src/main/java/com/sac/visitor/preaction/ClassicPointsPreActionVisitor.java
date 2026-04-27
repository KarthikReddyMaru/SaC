package com.sac.visitor.preaction;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ActionContext;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.*;
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
    public boolean visit(Drop drop, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        if (isActionIllegal(webSocketSession, username, gameState)) return false;

        Integer sourcePositionId = gameState.getActionPendingOn();
        Position sourcePosition = gameState.getPlayerPosition(username, sourcePositionId);

        if (sourcePosition.getActor() == null) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.noActorPresent(sourcePositionId));
            return false;
        } else if (!sourcePosition.getActor()
                                  .getAllowedActions()
                                  .contains(drop.getActionType())) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.actorCannotPerform(
                                                  sourcePosition.getActor()
                                                                .getCurrentState(),
                                                  drop.getActionType()));
            return false;
        }

        return true;
    }

    @Override
    public boolean visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getClientIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);

        if (isActionIllegal(webSocketSession, username, gameState)) {
            return false;
        } else {
            Actor actor = gameState.getPlayerPosition(username, gameState.getActionPendingOn())
                                          .getActor();
            if (actor != null) {
                String errorMsg = "An actor already present in this position, choose different action";
                messageService.sendSystemMessage(webSocketSession, errorMsg, ServerResponse.Type.ERROR);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean visit(Revert revert, WebSocketSession webSocketSession, ActionContext actionContext) {

        String username = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);

        if (isActionIllegal(webSocketSession, username, gameState)) {
            return false;
        }

        Position position = gameState.getPlayerPosition(username, gameState.getActionPendingOn());
        Actor actor = position.getActor();

        if (actionContext.getSourcePosition() != null && !isValidSourcePosition(webSocketSession, actionContext, gameState)) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            return false;
        } else if (actionContext.getDestinationPosition() != null && isInvalidDestinationPosition(webSocketSession, actionContext, gameState)) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            return false;
        }

        if (actor == null) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.noActorPresent(gameState.getActionPendingOn()));
            return false;
        } else if (!actor.getAllowedActions()
                         .contains(actionContext.getGameAction())) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.actorCannotPerform(actor.getCurrentState(),
                                                                                             actionContext.getGameAction()));
            return false;
        } else if (actionContext.getSourcePosition() != null && gameState.getActionPendingOn()
                                                                   .equals(actionContext.getSourcePosition())) {
            messageService.sendSystemMessage(webSocketSession, "You cannot perform Revert on the same position");
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(Promote promote, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        Specialization requestedTransition = actionContext.getSpecialization();
        GameState gameState = gameStateService.getGameState(roomId);

        if (isActionIllegal(webSocketSession, clientId, gameState)) return false;

        Position position = gameState.getPlayerPosition(clientId, gameState.getActionPendingOn());
        Actor actor = position.getActor();

        if (actor == null) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.noActorPresent(position.getPositionId()));
            return false;
        } else if (requestedTransition == null) {
            messageService.sendSystemMessage(webSocketSession, "Choose Specialization to evolve",
                                             ServerResponse.Type.ERROR);
            return false;
        } else if (!actor.getAllowedTransitions()
                         .contains(requestedTransition) || actor.getCurrentState()
                                                                .equals(requestedTransition)) {
            String errorMessage = String.format("%s cannot PROMOTE to %s", actor.getCurrentState(),
                                                requestedTransition);
            messageService.sendSystemMessage(webSocketSession, errorMessage, ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(Capture capture, WebSocketSession webSocketSession,
                         ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String username = SocketSessionUtil.getClientIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);
        Integer playerPositionId = gameState.getActionPendingOn();

        if (isActionIllegal(webSocketSession, username, gameState) ||
            isInvalidDestinationPosition(webSocketSession, actionContext, gameState)) return false;

        Position playerPosition = gameState.getPlayerPosition(username, playerPositionId);
        Actor actor = playerPosition.getActor();
        Integer opponentPositionId = actionContext.getDestinationPosition();
        String opponentId = actionContext.getDestinationPositionHolder();
        Position opponentPosition = gameState.getPlayer(opponentId).getPositions()[opponentPositionId];

        if (actor == null) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.noActorPresent(playerPosition.getPositionId()));
            return false;
        } else if (!actor.getAllowedActions()
                         .contains(capture.getActionType())) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.actorCannotPerform(actor.getCurrentState(),
                                                                           capture.getActionType()));
            return false;
        } else if (opponentPosition.isCapturedByOpponent()) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.capturedTrouble(username, opponentPositionId));
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(BlackOut blackOut, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String currentPlayerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);

        if (isActionIllegal(webSocketSession, currentPlayerId, gameState) ||
            isInvalidDestinationPosition(webSocketSession, actionContext, gameState)) return false;

        Integer destinationPositionId = actionContext.getDestinationPosition();
        String destinationPositionHolder = actionContext.getDestinationPositionHolder();

        Integer sourcePositionId = gameState.getActionPendingOn();
        Position destinationPosition = gameState.getPlayerPosition(destinationPositionHolder, destinationPositionId);
        Position currentPlayerPosition = gameState.getPlayerPosition(currentPlayerId, sourcePositionId);
        Actor currentPlayerPositionActor = currentPlayerPosition.getActor();

        if (destinationPosition.getActor() == null) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            return false;
        } else if (currentPlayerPositionActor == null) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.noActorPresent(sourcePositionId));
            return false;
        } else if (!currentPlayerPositionActor.getAllowedActions()
                                              .contains(blackOut.getActionType())) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.actorCannotPerform(currentPlayerPositionActor.getCurrentState(),
                                                                           blackOut.getActionType()));
            return false;
        }
        return true;
    }

    /**
     * Validates if the action requested by the player is legal based on the current game state.
     * If illegal, it notifies the player.
     *
     * @param session   The WebSocket session of the player attempting the action.
     * @param username  The username of the player attempting the action.
     * @param gameState The current state of the game room.
     * @return true if the action is illegal and should be blocked; false if the action can proceed.
     */
    private boolean isActionIllegal(WebSocketSession session, String username, GameState gameState) {
        boolean isCurrentPlayer = username.equals(gameState.getCurrentPlayerId());
        boolean hasPendingAction = gameState.isActionPending() && gameState.getActionPendingOn() != null;

        if (!isCurrentPlayer || !hasPendingAction) {
            messageService.sendRawPayload(session, MessageFormat.illegalAction());
            return true;
        }

        return false;
    }

    private boolean isInvalidDestinationPosition(WebSocketSession webSocketSession, ActionContext actionContext, GameState gameState) {
        String playerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String destinationPositionHolder = actionContext.getDestinationPositionHolder();
        boolean isValidPlayer = gameState.getPlayers()
                                         .stream()
                                         .anyMatch(player -> player.getClientId().equals(destinationPositionHolder));
        Integer destinationPosition = actionContext.getDestinationPosition();
        boolean isValidDestination = destinationPosition != null &&
                                     destinationPosition >= 1    &&
                                     destinationPosition <= this.maxPositionPerPlayer;
        if (!isValidPlayer || !isValidDestination || playerId.equals(destinationPositionHolder)) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            return true;
        }
        return false;
    }

    private boolean isValidSourcePosition(WebSocketSession webSocketSession, ActionContext actionContext, GameState gameState) {
        String sourcePositionHolder = actionContext.getSourcePositionHolder();
        boolean isValidPlayer = gameState.getPlayers()
                                         .stream()
                                         .anyMatch(player -> player.getClientId().equals(sourcePositionHolder));
        Integer sourcePosition = actionContext.getSourcePosition();
        boolean isValidSource = sourcePosition != null &&
                                     sourcePosition >= 1 &&
                                     sourcePosition <= this.maxPositionPerPlayer;
        if (!isValidPlayer || !isValidSource) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            return false;
        }
        return true;
    }
}
