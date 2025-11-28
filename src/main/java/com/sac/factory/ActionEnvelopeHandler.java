package com.sac.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.message.ActionContext;
import com.sac.model.message.MessageEnvelope;
import com.sac.model.message.MessageEnvelope.Type;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.GameplayService;
import com.sac.service.MessageService;
import com.sac.strategy.action.Action;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ActionEnvelopeHandler implements EnvelopeHandler {

    private final ObjectMapper objectMapper;
    private final ActionHandlerRegistry actionHandlerRegistry;
    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final GameplayService gameplayService;

    @Override
    public Type getType() {
        return Type.ACTION;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws IOException {
        // Clicking on action buttons before game init throwing exceptions, hence added check
        if (gameStateService.exists(roomId)) {
            ActionContext actionContext = objectMapper.treeToValue(messageEnvelope.getPayload(), ActionContext.class);
            Action action = actionHandlerRegistry.getInstance(actionContext.getGameAction());
            action.performAction(webSocketSession, actionContext, roomId);
            gameplayService.postProcessAction(webSocketSession, roomId);
        } else
            messageService.sendToSender(webSocketSession, "Game not initialized yet", ServerResponse.Type.ERROR);
    }
}
