package com.sac.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.GameState;
import com.sac.model.message.ActionContext;
import com.sac.model.message.MessageEnvelope;
import com.sac.model.message.MessageEnvelope.Type;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Action;
import com.sac.strategy.mode.Mode;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
        if (gameStateService.exists(roomId)) {
            ActionContext actionContext = objectMapper.treeToValue(messageEnvelope.getPayload(), ActionContext.class);
            Action action = actionHandlerRegistry.getInstance(actionContext.getGameAction());
            action.performAction(webSocketSession, actionContext, roomId);
            GameState gameState = gameStateService.getGameState(roomId);
            messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
            Mode mode = gameModeHandlerRegistry.getInstance(gameState.getGameMode());
            if (mode.computeWinner(roomId) != null) {
                String winner = SocketSessionUtil.getUserNameFromSession(webSocketSession);
                messageService.broadcastMessage(MessageFormat.endGameWithWinner(winner, gameState), roomId);
            }
        } else
            messageService.sendToSender(webSocketSession, "Game not initialized yet", ServerResponse.Type.ERROR);
    }
}
