package com.sac.factory;

import com.sac.model.Message.Type;
import com.sac.strategy.message.MessageHandlerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class MessageHandlerFactory {

    private final HashMap<Type, MessageHandlerStrategy> strategyMap;

    public MessageHandlerFactory(List<MessageHandlerStrategy> strategies) {
        strategyMap = new HashMap<>();
        for (MessageHandlerStrategy strategy : strategies) {
            Type type = strategy.getStrategy();
            strategyMap.put(type, strategy);
        }
    }

    public MessageHandlerStrategy getInstance(Type type) {
        if (!strategyMap.containsKey(type)) {
            log.warn("Invalid strategy");
            return null;
        }
        return strategyMap.get(type);
    }

}
