package com.sac.strategy.message;

import com.sac.model.Message;
import com.sac.service.MessageService;
import com.sac.service.RoomConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WaitingResponseStrategy implements MessageHandlerStrategy {

    private final MessageService messageService;
    private final RoomConnectionService roomConnectionService;
    private final ConcurrentHashMap<String, HashMap<WebSocketSession, String>> receivedResponsesMap = new ConcurrentHashMap<>();

    @Override
    public void handle(WebSocketSession webSocketSession, Message message, String roomId) throws IOException {
        // Check roomId before calling this
        receivedResponsesMap.computeIfAbsent(roomId, (room) -> new HashMap<>());
        HashMap<WebSocketSession, String> receivedResponses = receivedResponsesMap.get(roomId);
        if (!receivedResponses.containsKey(webSocketSession)) {
            receivedResponsesMap.get(roomId).put(webSocketSession, message.getContent());
            if (areAllResponsesReceived(roomId)) {
                messageService.broadcastMessage("Both players are responded!", roomId);
                receivedResponsesMap.remove(roomId);
            }
        }
        else
            messageService.sendToSender(webSocketSession, "Hey! your response is already recorded, waiting for opponent");
    }

    @Override
    public Message.Type getStrategy() {
        return Message.Type.WAITING_FOR_RESPONSE;
    }

    private boolean areAllResponsesReceived(String roomId) {
        int roomSize = roomConnectionService.getRoomSize(roomId);
        int responsesReceived = receivedResponsesMap.get(roomId).size();
        return roomSize == responsesReceived;
    }
}
