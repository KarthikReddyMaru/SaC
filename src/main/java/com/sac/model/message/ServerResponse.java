package com.sac.model.message;

public record ServerResponse(Type type, String sender, Object content) {
    public enum Type {
        MESSAGE,
        SELECT_POSITION,
        ACTION_REQUIRED,
        INFO,
        STATE,
        ERROR,
        FINISH
    }
}
