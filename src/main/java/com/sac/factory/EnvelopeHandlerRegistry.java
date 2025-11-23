package com.sac.factory;

import com.sac.model.message.MessageEnvelope.Type;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EnvelopeHandlerRegistry {

    private final Map<Type, EnvelopeHandler> registry = new HashMap<>();

    public EnvelopeHandlerRegistry(List<EnvelopeHandler> envelopeHandlers) {
        for (EnvelopeHandler envelopeHandler : envelopeHandlers) {
            registry.put(envelopeHandler.getType(), envelopeHandler);
        }
    }

    public EnvelopeHandler getInstance(Type type) {
        if (!registry.containsKey(type))
            throw new IllegalArgumentException("Invalid type of envelope "+type.name());
        return registry.get(type);
    }

}
