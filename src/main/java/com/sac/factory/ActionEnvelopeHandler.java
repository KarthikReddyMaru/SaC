package com.sac.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.GameMode;
import com.sac.model.message.ActionContext;
import com.sac.model.message.MessageEnvelope;
import com.sac.model.message.MessageEnvelope.Type;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.strategy.mode.Mode;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ActionEnvelopeHandler implements EnvelopeHandler {

    private final ObjectMapper objectMapper;
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
            GameMode gameMode = GameMode.fromString(SocketSessionUtil.getGameMode(webSocketSession));
            Mode mode = gameModeHandlerRegistry.getInstance(gameMode);
            mode.performAction(webSocketSession, actionContext, roomId);
        } else
            messageService.sendToSender(webSocketSession, "Game not initialized yet", ServerResponse.Type.ERROR);
    }
}
