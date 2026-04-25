package com.sac.strategy.action;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.util.SocketSessionUtil;
import com.sac.visitor.postaction.PostActionVisitor;
import com.sac.visitor.preaction.PreActionVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.sac.strategy.action.GameAction.BLACKOUT;

@Component
@RequiredArgsConstructor
public class BlackOut implements Action {

    private final GameStateService gameStateService;

    @Override
    public boolean preAction(PreActionVisitor preActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext) {
        return preActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {

        GameState gameState = gameStateService.getGameState(roomId);
        Integer destinationPositionId = actionContext.getDestinationPosition();
        Integer sourcePositionId = gameState.getActionPendingOn();
        String currentPlayerId = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        String destinationPositionHolder = actionContext.getDestinationPositionHolder();

        Position destinationPosition = gameState.getPlayerPosition(destinationPositionHolder, destinationPositionId);
        Position currentPlayerPosition = gameState.getPlayerPosition(currentPlayerId, sourcePositionId);
        Specialization destinationPositionActorState = destinationPosition.getActor().getCurrentState();

        Position[] opponentPositions = gameState.getPlayer(destinationPositionHolder).getPositions();
        for (Position opponentPosition : opponentPositions) {
            Actor actor = opponentPosition.getActor();
            if (actor != null && actor.getCurrentState().equals(destinationPositionActorState))
                opponentPosition.setActor(null);
        }
        currentPlayerPosition.setActor(null);
    }

    @Override
    public void postAction(PostActionVisitor postActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext) {
        postActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 0;
    }

    @Override
    public GameAction getActionType() {
        return BLACKOUT;
    }
}
