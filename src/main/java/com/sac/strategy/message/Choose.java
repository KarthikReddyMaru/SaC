package com.sac.strategy.message;

import com.sac.model.GameState;
import com.sac.model.message.DefaultMessage;
import com.sac.model.message.DefaultMessage.Type;
import com.sac.model.message.ServerResponse;
import com.sac.service.ChosenResponseService;
import com.sac.service.GameStateService;
import com.sac.service.MessageService;
import com.sac.util.MessageFormat;
import com.sac.util.SocketSessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class Choose implements MessageHandlerStrategy {

    private final Map<String, Map<String, Integer>> roomsRespondedPlayers = new ConcurrentHashMap<>();

    private final GameStateService gameStateService;
    private final MessageService messageService;
    private final ChosenResponseService chosenResponseService;

    @Override
    public Type getStrategy() {
        return Type.CHOOSE;
    }

    @Override
    public void handle(WebSocketSession webSocketSession, DefaultMessage message, String roomId) throws IOException {
        GameState gameState = gameStateService.getGameState(roomId);
        if (preProcessChecks(webSocketSession, message.getContent(), gameState)) {
            roomsRespondedPlayers.computeIfAbsent(roomId, (room) -> new HashMap<>());
            String respondedPlayerId = SocketSessionUtil.getUserNameFromSession(webSocketSession);
            Map<String, Integer> respondedPlayers = roomsRespondedPlayers.get(roomId);
            if (respondedPlayers.containsKey(respondedPlayerId)) {
                messageService.sendToSender(
                        webSocketSession,
                        "Your response is already recorded, wait for opponent",
                        ServerResponse.Type.ERROR);
            } else {
                respondedPlayers.put(respondedPlayerId, Integer.parseInt(message.getContent()));
                messageService.sendToSender(webSocketSession, "Your response is recorded as " + message.getContent());
                messageService.sendMessage(webSocketSession, MessageFormat.chosenResponseMessage(respondedPlayerId), roomId);
                int totalPlayersInTheRoom = gameState.getPlayerCount();
                if (totalPlayersInTheRoom == respondedPlayers.size())
                    chosenResponseService.processChosenResponses(roomId, gameState, respondedPlayers);
            }
        }
    }

    private boolean preProcessChecks(WebSocketSession webSocketSession, String chosenNumber, GameState gameState) {
        if (gameState.isActionPending()) {
            String errorMsg = String.format("%s needs to perform action before choosing",
                            gameState.getCurrentPlayerId());
            messageService.sendToSender(webSocketSession,
                    MessageFormat.systemError(errorMsg),
                    ServerResponse.Type.ERROR);
            return false;
        }
        if (!Pattern.matches("^[0-5]$", chosenNumber)) {
            String errorMsg = "Only positions from 0 to 5 are allowed";
            messageService.sendToSender(webSocketSession,
                    MessageFormat.systemError(errorMsg),
                    ServerResponse.Type.ERROR);
            return false;
        }
        return true;
    }

}
