package com.sac.model.message;

public record ServerResponse(Type type, String sender, Object content) {
    public enum Type {
        MESSAGE,

        INFO,
        ERROR,

        STATE,
        FINISH,

        POSITION_SELECTION
    }
}
