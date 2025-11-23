package com.sac.strategy.message;

import com.sac.model.message.DefaultMessage;
import com.sac.model.message.DefaultMessage.Type;
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
    public void handle(WebSocketSession webSocketSession, DefaultMessage message, String roomId) throws IOException {
        messageService.sendMessage(webSocketSession, message, roomId);
    }

    @Override
    public Type getStrategy() {
        return Type.CHAT;
    }
}
