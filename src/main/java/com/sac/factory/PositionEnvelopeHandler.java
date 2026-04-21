package com.sac.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.GameMode;
import com.sac.model.message.MessageEnvelope;
import com.sac.model.message.MessageEnvelope.Type;
import com.sac.model.message.PositionContext;
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
public class PositionEnvelopeHandler implements EnvelopeHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final GameModeHandlerRegistry gameModeHandlerRegistry;


    @Override
    public Type getType() {
        return Type.POSITION;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws IOException {
        PositionContext positionContext = objectMapper.treeToValue(messageEnvelope.getPayload(), PositionContext.class);
        if (gameStateService.exists(roomId)) {
            GameMode gameMode = GameMode.fromString(SocketSessionUtil.getGameMode(webSocketSession));
            Mode mode = gameModeHandlerRegistry.getInstance(gameMode);
            mode.performChoose(webSocketSession, positionContext);
        } else
            messageService.sendToSender(webSocketSession, "Game not initialized yet", ServerResponse.Type.ERROR);
    }
}
