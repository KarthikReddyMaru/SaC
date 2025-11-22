package com.sac.model.actor;

import com.sac.model.GameState;
import com.sac.model.Position;

public interface Action {
    GameAction getActionType();
    void performAction(Position sourcePosition, Position targetPosition);
}
