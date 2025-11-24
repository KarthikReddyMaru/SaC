package com.sac.strategy.action;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.message.ActionContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class AttackAndCapture implements Action {

    @Override
    public GameAction getActionType() {
        return GameAction.ATTACK_AND_CAPTURE;
    }

    @Override
    public void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) {

    }

    public boolean preProcessChecks(WebSocketSession webSocketSession, String username, GameState gameState, Position position) {
        return false;
    }
}
