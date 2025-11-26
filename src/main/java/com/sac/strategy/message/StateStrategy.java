package com.sac.strategy.message;

import com.sac.model.GameState;
import com.sac.model.message.DefaultMessage;
import com.sac.model.message.DefaultMessage.Type;
import com.sac.service.GameRendererService;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StateStrategy implements MessageHandlerStrategy {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    @Override
    public void handle(WebSocketSession webSocketSession, DefaultMessage message, String roomId) throws IOException {
        GameState gameState = gameStateService.getGameState(roomId);
        messageService.sendToSender(webSocketSession, GameRendererService.render(gameState));
    }

    @Override
    public Type getStrategy() {
        return Type.STATE;
    }
}
