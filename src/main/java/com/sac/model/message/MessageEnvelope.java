package com.sac.model.message;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class MessageEnvelope {

    private Type type;
    private JsonNode payload;

    public enum Type {
        ACTION, GAME
    }
}
