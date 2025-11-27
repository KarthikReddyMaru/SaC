package com.sac.model.message;

public record ServerResponse(Type type, String sender, String content) {
    public enum Type {
        MESSAGE, INFO, STATE, ERROR, FINISH
    }
}
