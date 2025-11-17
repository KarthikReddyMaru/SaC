package com.sac.strategy.message;

import com.sac.model.Message;
import com.sac.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DefaultMessageStrategy implements MessageHandlerStrategy {

    private final MessageService messageService;

    @Override
    public void handle(WebSocketSession webSocketSession, Message message, String roomId) throws IOException {
        messageService.sendMessage(webSocketSession, message, roomId);
    }

    @Override
    public Message.Type getStrategy() {
        return Message.Type.MESSAGE;
    }
}
