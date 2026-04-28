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
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
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

    @Override @WithSpan("envelope.position")
    public void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws IOException {
        PositionContext positionContext = objectMapper.treeToValue(messageEnvelope.getPayload(), PositionContext.class);
        log.info("Received: POSITION envelope");
        if (gameStateService.exists(roomId)) {
            GameMode gameMode = GameMode.fromString(SocketSessionUtil.getGameMode(webSocketSession));
            Mode mode = gameModeHandlerRegistry.getInstance(gameMode);
            setSpanAttributes(Span.current(), positionContext);
            mode.performChoose(webSocketSession, positionContext);
        } else
            messageService.sendSystemMessage(webSocketSession, "Game not initialized yet", ServerResponse.Type.ERROR);
    }

    private void setSpanAttributes(Span span, PositionContext positionContext) {
        span.setAttribute("position", positionContext.getPosition());
    }
}
