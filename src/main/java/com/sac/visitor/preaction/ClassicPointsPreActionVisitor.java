package com.sac.visitor.preaction;

import com.sac.config.actor.ActorEvolutionConfig;
import com.sac.model.GameMode;
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
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.sac.model.GameMode.CLASSIC_POINTS;
import static com.sac.model.GameState.GameplayStatus.INIT;
import static com.sac.model.GameState.GameplayStatus.OFFLINE;
import static com.sac.model.actor.Specialization.RECRUIT;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("LoggingSimilarMessage")
public class ClassicPointsPreActionVisitor implements PreActionVisitor {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Value("${player.total_positions}")
    private int maxPositionPerPlayer;

    @Override
    public GameMode getMode() {
        return CLASSIC_POINTS;
    }

    @Override
    @WithSpan("preaction.drop")
    public boolean visit(Drop drop, WebSocketSession webSocketSession, ActionContext actionContext) {

        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        GameState gameState = gameStateService.getGameState(roomId);

        addActionContextToSpanAttributes(actionContext);

        boolean flag =  gameState.getCurrentPlayerId().equals(clientId);
        if (!flag)
            log.warn("Cannot drop turn when it is not yours");
        return flag;
    }

    @Override
    @WithSpan("preaction.spawn")
    public boolean visit(Spawn spawn, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);
        addActionContextToSpanAttributes(actionContext);

        if (isActionIllegal(webSocketSession, clientId, gameState)) {
            return false;
        }

        if (!actionContext.getSpecialization().equals(RECRUIT)) {
            String msg = "Cannot spawn this unit now";
            messageService.sendRawPayload(webSocketSession, MessageFormat.systemError(msg));
            log.warn(msg);
            return false;
        }

        Actor actor = gameState.getPlayerPosition(clientId, gameState.getActionPendingOn())
                               .getActor();
        if (actor != null) {
            String errorMsg = "An actor already present in this position, choose different action";
            messageService.sendSystemMessage(webSocketSession, errorMsg, ServerResponse.Type.ERROR);
            log.warn(errorMsg);
            return false;

        }
        return true;
    }

    @Override
    @WithSpan("preaction.revert")
    public boolean visit(Revert revert, WebSocketSession webSocketSession, ActionContext actionContext) {

        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);
        addActionContextToSpanAttributes(actionContext);

        if (isActionIllegal(webSocketSession, clientId, gameState)) {
            return false;
        }

        Position position = gameState.getPlayerPosition(clientId, gameState.getActionPendingOn());
        Actor actor = position.getActor();

        if (actionContext.getSourcePosition() != null && !isValidSourcePosition(webSocketSession, actionContext,
                                                                                gameState)) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            log.warn("Invalid source position or holder");
            return false;
        } else if (actionContext.getDestinationPosition() != null && isInvalidDestinationPosition(webSocketSession,
                                                                                                  actionContext,
                                                                                                  gameState)) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            log.warn("Invalid destination position or holder");
            return false;
        }

        if (actor == null) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.noActorPresent(gameState.getActionPendingOn()));
            log.warn("No actor present at {}", gameState.getActionPendingOn());
            return false;
        } else if (!actor.getAllowedActions(this.getMode())
                         .contains(actionContext.getGameAction())) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.actorCannotPerform(actor.getCurrentState(),
                                                                                             actionContext.getGameAction()));
            log.warn("{} cannot perform {}", actor.getCurrentState()
                                                  .name(), revert.getActionType()
                                                                 .name());
            return false;
        } else if (actionContext.getSourcePosition() != null && gameState.getActionPendingOn()
                                                                         .equals(actionContext.getSourcePosition())) {
            String msg = "You cannot perform Revert on the same position";
            messageService.sendSystemMessage(webSocketSession, msg);
            log.warn(msg);
            return false;
        }
        return true;
    }

    @Override
    @WithSpan("preaction.promote")
    public boolean visit(Promote promote, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        Specialization requestedTransition = actionContext.getSpecialization();
        GameState gameState = gameStateService.getGameState(roomId);

        addActionContextToSpanAttributes(actionContext);

        if (isActionIllegal(webSocketSession, clientId, gameState)) return false;

        Position position = gameState.getPlayerPosition(clientId, gameState.getActionPendingOn());
        Actor actor = position.getActor();

        if (actor == null) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.noActorPresent(position.getPositionId()));
            log.warn("No actor present at {}", gameState.getActionPendingOn());
            return false;
        } else if (requestedTransition == null) {
            String msg = "Choose Specialization to evolve";
            messageService.sendSystemMessage(webSocketSession, MessageFormat.systemError(msg));
            log.warn(msg);
            return false;
        } else if (!actor.getAllowedTransitions(this.getMode())
                         .contains(requestedTransition) || actor.getCurrentState()
                                                                .equals(requestedTransition)) {
            String errorMessage = String.format("%s cannot PROMOTE to %s", actor.getCurrentState(),
                                                requestedTransition);
            messageService.sendSystemMessage(webSocketSession, MessageFormat.systemError(errorMessage));
            log.warn(errorMessage);
            return false;
        }
        return true;
    }

    @Override
    @WithSpan("preaction.capture")
    public boolean visit(Capture capture, WebSocketSession webSocketSession,
                         ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String clientId = SocketSessionUtil.getClientIdFromSession(webSocketSession);

        GameState gameState = gameStateService.getGameState(roomId);
        Integer playerPositionId = gameState.getActionPendingOn();

        addActionContextToSpanAttributes(actionContext);

        if (isActionIllegal(webSocketSession, clientId, gameState) ||
            isInvalidDestinationPosition(webSocketSession, actionContext, gameState)) return false;

        Position playerPosition = gameState.getPlayerPosition(clientId, playerPositionId);
        Actor actor = playerPosition.getActor();
        Integer opponentPositionId = actionContext.getDestinationPosition();
        String opponentId = actionContext.getDestinationPositionHolder();
        Position opponentPosition = gameState.getPlayer(opponentId)
                                             .getPositions()[opponentPositionId];

        if (actor == null) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.noActorPresent(playerPosition.getPositionId()));
            log.warn("No actor present at {}", playerPositionId);
            return false;
        } else if (!actor.getAllowedActions(this.getMode())
                         .contains(capture.getActionType())) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.actorCannotPerform(actor.getCurrentState(),
                                                                           capture.getActionType()));
            log.warn("{} cannot perform {}", actor.getCurrentState()
                                                  .name(), capture.getActionType()
                                                                  .name());
            return false;
        } else if (opponentPosition.isCapturedByOpponent()) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.capturedTrouble(clientId, opponentPositionId));
            log.warn("Invalid destination position, Position is already captured");
            return false;
        }
        return true;
    }

    @Override
    @WithSpan("preaction.blackout")
    public boolean visit(BlackOut blackOut, WebSocketSession webSocketSession, ActionContext actionContext) {

        String roomId = SocketSessionUtil.getRoomIdFromSession(webSocketSession);
        String currentPlayerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);

        addActionContextToSpanAttributes(actionContext);

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
            log.warn("Invalid destination, No actor present in destination position");
            return false;
        } else if (currentPlayerPositionActor == null) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.noActorPresent(sourcePositionId));
            log.warn("No actor present at {}", sourcePositionId);
            return false;
        } else if (!currentPlayerPositionActor.getAllowedActions(this.getMode())
                                              .contains(blackOut.getActionType())) {
            messageService.sendRawPayload(webSocketSession,
                                          MessageFormat.actorCannotPerform(currentPlayerPositionActor.getCurrentState(),
                                                                           blackOut.getActionType()));
            log.warn("{} cannot perform {}", currentPlayerPositionActor.getCurrentState()
                                                                       .name(), blackOut.getActionType()
                                                                                        .name());
            return false;
        }
        return true;
    }

    /**
     * Validates if the action requested by the player is legal based on the current game state.
     * If illegal, it notifies the player.
     *
     * @param session   The WebSocket session of the player attempting the action.
     * @param clientId  The clientId of the player attempting the action.
     * @param gameState The current state of the game room.
     * @return true if the action is illegal and should be blocked; false if the action can proceed.
     */
    private boolean isActionIllegal(WebSocketSession session, String clientId, GameState gameState) {

        if (gameState.getGameplayStatus()
                     .equals(INIT)) {

            String msg = "Game not initialized";
            messageService.sendRawPayload(session, MessageFormat.systemError(msg));

            log.warn(msg);
            Span.current()
                .addEvent("game_uninitialized")
                .setAttribute("trace_reason", msg);

            return true;

        } else if (gameState.getGameplayStatus()
                            .equals(OFFLINE)) {

            String msg = "Wait till opponent returns";
            messageService.sendRawPayload(session, MessageFormat.systemError(msg));

            log.warn(msg);
            Span.current()
                .addEvent("game_uninitialized")
                .setAttribute("trace_reason", msg);

            return true;
        }

        boolean isCurrentPlayer = clientId.equals(gameState.getCurrentPlayerId());
        boolean hasPendingAction = gameState.isActionPending() && gameState.getActionPendingOn() != null;

        if (!isCurrentPlayer || !hasPendingAction) {
            messageService.sendRawPayload(session, MessageFormat.illegalAction());
            log.warn("Illegal action");
            Span.current()
                .addEvent("illegal_action")
                .setAttribute("clientId", clientId);
            return true;
        }

        return false;
    }

    private boolean isInvalidDestinationPosition(WebSocketSession webSocketSession, ActionContext actionContext,
                                                 GameState gameState) {
        String playerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String destinationPositionHolder = actionContext.getDestinationPositionHolder();
        boolean isValidPlayer = gameState.getPlayers()
                                         .stream()
                                         .anyMatch(player -> player.getClientId()
                                                                   .equals(destinationPositionHolder));
        Integer destinationPosition = actionContext.getDestinationPosition();
        boolean isValidDestination = destinationPosition != null &&
                                     destinationPosition >= 1 &&
                                     destinationPosition <= this.maxPositionPerPlayer;
        if (!isValidPlayer || !isValidDestination || playerId.equals(destinationPositionHolder)) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            log.warn("Invalid destination or destination position holder");
            return true;
        }
        return false;
    }

    private boolean isValidSourcePosition(WebSocketSession webSocketSession, ActionContext actionContext,
                                          GameState gameState) {
        String sourcePositionHolder = actionContext.getSourcePositionHolder();
        boolean isValidPlayer = gameState.getPlayers()
                                         .stream()
                                         .anyMatch(player -> player.getClientId()
                                                                   .equals(sourcePositionHolder));
        Integer sourcePosition = actionContext.getSourcePosition();
        boolean isValidSource = sourcePosition != null &&
                                sourcePosition >= 1 &&
                                sourcePosition <= this.maxPositionPerPlayer;
        if (!isValidPlayer || !isValidSource) {
            messageService.sendRawPayload(webSocketSession, MessageFormat.inValidDestinationProvided());
            log.warn("Invalid source position or source position holder");
            return false;
        }
        return true;
    }

    private void addActionContextToSpanAttributes(ActionContext actionContext) {
        String gameAction = actionContext.getGameAction() != null ? actionContext.getGameAction()
                                                                                 .name() : null;
        int sourcePosition = actionContext.getSourcePosition() != null ? actionContext.getSourcePosition() : 0;
        int destinationPosition = actionContext.getSourcePosition() != null ? actionContext.getSourcePosition() : 0;
        String sourcePositionHolder = actionContext.getSourcePositionHolder();
        String destinationPositionHolder = actionContext.getDestinationPositionHolder();

        Span.current()
            .setAttribute("gameAction", gameAction)
            .setAttribute("sourcePosition", sourcePosition)
            .setAttribute("destinationPosition", destinationPosition)
            .setAttribute("sourcePositionHolder", sourcePositionHolder)
            .setAttribute("destinationPositionHolder", destinationPositionHolder);
    }
}
