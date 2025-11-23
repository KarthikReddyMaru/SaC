package com.sac.model.actor;

import com.sac.strategy.action.Action;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@SuperBuilder(toBuilder = true)
public abstract class Actor {

    @Builder.Default
    protected final int cooldown = 0;

    boolean canPerformAction() {
        return cooldown == 0;
    }

    public int coolDownLeft() {
        return cooldown;
    }

    abstract Actor withCoolDown(int coolDownSteps);
    abstract Actor withDecrementedCoolDown();
    abstract Set<Specialization> getAllowedTransitions();
    abstract Set<Action> getAllowedActions();
    abstract Specialization getCurrentState();
}
