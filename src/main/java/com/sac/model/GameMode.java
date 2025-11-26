package com.sac.model;

import java.util.Arrays;

public enum GameMode {
    POINTS;

    public static GameMode fromString(String gameMode) {
        return Arrays.stream(GameMode.values())
                .filter(mode -> mode.name().equalsIgnoreCase(gameMode))
                .findFirst()
                .orElse(POINTS);
    }
}
