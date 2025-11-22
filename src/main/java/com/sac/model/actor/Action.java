package com.sac.model.actor;

import com.sac.model.GameState;

public interface Action {
    GameAction getActionType();
    void performAction(GameState gameState);
}
