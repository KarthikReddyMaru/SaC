package com.sac.strategy.action;

import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.model.message.ActionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import com.sac.visitor.postaction.PostActionVisitor;
import com.sac.visitor.preaction.PreActionVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.sac.strategy.action.GameAction.KAMIKAZE;

@Component
@RequiredArgsConstructor
public class Kamikaze implements Action {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public GameAction getActionType() {
        return KAMIKAZE;
    }

    @Override
    public void preAction(PreActionVisitor preActionVisitor, WebSocketSession webSocketSession, ActionContext actionContext) {
        preActionVisitor.visit(this, webSocketSession, actionContext);
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {

        GameState gameState = gameStateService.getGameState(roomId);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        Player opponent = gameState.getOpponent(username);

        Integer destinationPositionToPerformAction = actionContext.getDestinationPosition();
        Integer sourcePositionToPerformAction = actionContext.getSourcePosition();
        Integer actionPendingPosition = gameState.getActionPendingOn();

        Position playerPosition = gameState.getPlayerPosition(username, actionPendingPosition);
        Position opponentPosition = opponent.getPositions()[destinationPositionToPerformAction];

        if (sourcePositionToPerformAction != null) {
            playerPosition.restorePosition();
            messageService.broadcastMessage(
                    MessageFormat.kamikazeSuccessAction(username, sourcePositionToPerformAction), roomId);
        } else {
            opponentPosition.restorePosition();
            messageService.broadcastMessage(
                    MessageFormat.kamikazeSuccessAction(opponent.getUsername(), destinationPositionToPerformAction),
                    roomId);
        }
    }

    @Override
    public void postAction(PostActionVisitor postActionVisitor, WebSocketSession webSocketSession) {
        postActionVisitor.visit(this, webSocketSession);
    }

    @Override
    public int pointsForSuccessfulAction() {
        return 1;
    }
}
