package com.sac.strategy.action;

import com.sac.model.Position;
import com.sac.model.actor.Action;
import com.sac.model.actor.GameAction;
import com.sac.util.exception.IllegalMoveException;
import org.springframework.stereotype.Component;

@Component
public class AttackAndCapture implements Action {

    @Override
    public GameAction getActionType() {
        return GameAction.ATTACK_AND_CAPTURE;
    }

    @Override
    public void performAction(Position sourcePosition, Position targetPosition) {
        if (sourcePosition.getBelongsTo().equals(targetPosition.getBelongsTo()))
            throw new IllegalMoveException("Invalid position to capture");
        String user = sourcePosition.getBelongsTo();
        targetPosition.capturePosition(user, true);
    }
}
