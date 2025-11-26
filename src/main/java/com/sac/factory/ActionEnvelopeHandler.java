package com.sac.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.GameState;
import com.sac.model.message.ActionContext;
import com.sac.model.message.MessageEnvelope;
import com.sac.model.message.MessageEnvelope.Type;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Action;
import com.sac.strategy.action.GameAction;
import com.sac.strategy.mode.Mode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ActionEnvelopeHandler implements EnvelopeHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ActionHandlerRegistry actionHandlerRegistry;
    private final GameModeHandlerRegistry gameModeHandlerRegistry;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public Type getType() {
        return Type.ACTION;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws IOException {
        ActionContext actionContext = objectMapper.treeToValue(messageEnvelope.getPayload(), ActionContext.class);
        Action action = actionHandlerRegistry.getInstance(actionContext.getGameAction());
        action.performAction(webSocketSession, actionContext, roomId);
        GameState gameState = gameStateService.getGameState(roomId);
        Mode mode = gameModeHandlerRegistry.getInstance(gameState.getGameMode());
        if (mode.computeWinner(roomId) != null) {
            messageService.broadcastMessage(gameState.getCurrentPlayerId().concat(" won."), roomId);
            webSocketSession.close(CloseStatus.NORMAL);
        }
    }
}
