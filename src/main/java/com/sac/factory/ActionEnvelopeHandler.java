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
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
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
    @WithSpan("envelope.action")
    public void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws Exception {

        if (gameStateService.exists(roomId)) {
            ActionContext actionContext = objectMapper.treeToValue(messageEnvelope.getPayload(),
                                                                   ActionContext.class);
            log.info("Received: {} action", actionContext.getGameAction());
            GameMode gameMode = GameMode.fromString(SocketSessionUtil.getGameMode(webSocketSession));
            Mode mode = gameModeHandlerRegistry.getInstance(gameMode);
            setSpanAttributes(Span.current(), actionContext);
            mode.performAction(webSocketSession, actionContext, roomId);
        } else
            messageService.sendRawPayload(webSocketSession, MessageFormat.systemError("Game not initialized yet"));

    }

    private void setSpanAttributes(Span span, ActionContext actionContext) {
        if (actionContext.getGameAction() != null)
            span.setAttribute("action", actionContext.getGameAction()
                                                     .name());
        if (actionContext.getSpecialization() != null)
            span.setAttribute("upgrade", actionContext.getSpecialization()
                                                      .name());
        if (actionContext.getSourcePosition() != null)
            span.setAttribute("sourcePosition", actionContext.getSourcePosition());
        if (actionContext.getDestinationPosition() != null)
            span.setAttribute("destinationPosition", actionContext.getDestinationPosition());
        span.setAttribute("sourcePositionHolder", actionContext.getSourcePositionHolder());
        span.setAttribute("destinationPositionHolder", actionContext.getDestinationPositionHolder());
    }
}
