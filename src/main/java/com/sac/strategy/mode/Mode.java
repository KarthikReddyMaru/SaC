package com.sac.strategy.mode;

import com.sac.model.GameMode;

public interface Mode {

    String computeWinner(String roomId);
    GameMode getMode();

}
