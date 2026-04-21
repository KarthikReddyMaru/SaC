package com.sac.strategy.mode;

import com.sac.model.GameMode;
import com.sac.model.message.ActionContext;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface Mode {

    String computeWinner(String roomId);
    GameMode getMode();
    default void performAction(WebSocketSession webSocketSession, ActionContext actionContext, String roomId) throws IOException {}
}
