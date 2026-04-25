package com.sac.factory;

import com.sac.model.GameState;
import com.sac.model.message.MessageEnvelope;
import com.sac.model.message.PositionSelectionContext;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Random;

import static com.sac.model.message.MessageEnvelope.Type.POSITION_SELECTION;
import static com.sac.strategy.position.PositionSelection.ROLL;

@Component
@RequiredArgsConstructor
public class PositionSelectionEnvelopeHandler implements EnvelopeHandler {

    private final GameStateService gameStateService;
    private final Random random = new Random();
    private final char[] diceFaces = {'1', '2', '3', '4', '5', '\0'};
    private final MessageService messageService;

    @Override
    public MessageEnvelope.Type getType() {
        return POSITION_SELECTION;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, MessageEnvelope messageEnvelope, String roomId) throws IOException {
        GameState gameState = gameStateService.getGameState(roomId);
        String username = SocketSessionUtil.getUserNameFromSession(webSocketSession);
        if (gameState != null &&
            gameState.getCurrentPlayerId()
                     .equals(username) &&
            gameState.getState()
                     .equals(GameState.State.ROLL)) {
            if (gameState.getPositionSelection()
                         .equals(ROLL)) {
                int i1 = random.nextInt(0, diceFaces.length + 1);
                int i2 = random.nextInt(0, diceFaces.length + 1);
                PositionSelectionContext positionSelectionContext = new PositionSelectionContext(
                        new char[]{diceFaces[i1], diceFaces[i2]}, i1 == i2 && i1 == '\0');
                messageService.broadcastMessage(MessageFormat.rollResult(positionSelectionContext), roomId);
            }
        }
    }
}
