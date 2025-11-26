package com.sac.factory;

import com.sac.model.GameMode;
import com.sac.strategy.mode.Mode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameModeHandlerRegistry {

    private final Map<GameMode, Mode> gameModeRegistry = new ConcurrentHashMap<>();

    public GameModeHandlerRegistry(List<Mode> modes) {
        for (Mode mode : modes) {
            gameModeRegistry.put(mode.getMode(), mode);
        }
    }

    public Mode getInstance(GameMode gameMode) {
        if (!gameModeRegistry.containsKey(gameMode))
            throw new IllegalArgumentException("Invalid Game mode");
        return gameModeRegistry.get(gameMode);
    }

}
