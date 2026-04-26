package com.sac.strategy.action;

import java.util.Arrays;

public enum GameAction {
    DROP,
    SPAWN,
    PROMOTE,
    REVERT,
    CAPTURE,
    BLACKOUT;

    public static GameAction fromString(String gameAction) {
        return Arrays.stream(GameAction.values())
                .filter(action -> gameAction.equalsIgnoreCase(action.name()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
