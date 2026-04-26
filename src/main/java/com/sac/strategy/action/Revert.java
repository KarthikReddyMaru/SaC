package com.sac.strategy.action;

import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.util.SocketSessionUtil;
import com.sac.visitor.postaction.PostActionVisitor;
import com.sac.visitor.preaction.PreActionVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.sac.strategy.action.GameAction.REVERT;

@Component
@RequiredArgsConstructor
public class Revert implements Action {

    private final GameStateService gameStateService;

    @Override
    public GameAction getActionType() {
        return REVERT;
    }

    @Override
    public boolean preAction(PreActionVisitor preActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext) {
        return preActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {

        GameState gameState = gameStateService.getGameState(roomId);
        String playerId = SocketSessionUtil.getClientIdFromSession(webSocketSession);
        String opponentPlayerId = actionContext.getDestinationPositionHolder();

        Integer destinationPositionToPerformAction = actionContext.getDestinationPosition();
        Integer sourcePositionToPerformAction = actionContext.getSourcePosition();

        Integer actionPendingPosition = gameState.getActionPendingOn();
        Position actionPerformingPosition = gameState.getPlayerPosition(playerId, actionPendingPosition);

        actionPerformingPosition.setActor(null);
        if (sourcePositionToPerformAction != null) {
            gameState.getPlayerPosition(playerId, sourcePositionToPerformAction).restorePosition();
        } else {
            gameState.getPlayerPosition(opponentPlayerId, destinationPositionToPerformAction).restorePosition();
        }
    }

    @Override
    public void postAction(PostActionVisitor postActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext) {
        postActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 1;
    }
}
