package com.sac.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.message.ActionContext;
import com.sac.model.message.MessageEnvelope;
import com.sac.model.message.MessageEnvelope.Type;
import com.sac.strategy.action.Action;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ActionEnvelopeHandler implements EnvelopeHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ActionHandlerRegistry actionHandlerRegistry;

    @Override
    public Type getType() {
        return Type.ACTION;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws IOException {
        ActionContext actionContext = objectMapper.treeToValue(messageEnvelope.getPayload(), ActionContext.class);
        Action action = actionHandlerRegistry.getInstance(actionContext.getGameAction());
        action.performAction(webSocketSession, actionContext, roomId);
    }
}
