package com.sac.strategy.action;

import com.sac.model.actor.GameAction;
import com.sac.model.message.ActionContext;
import org.springframework.web.socket.WebSocketSession;

public interface Action {
    GameAction getActionType();
    void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId);
}
