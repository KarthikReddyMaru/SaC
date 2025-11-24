package com.sac.model.actor;

import com.sac.strategy.action.Action;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@SuperBuilder(toBuilder = true)
public abstract class Actor {

    @Builder.Default
    protected boolean isFrozen = false;

    public boolean canPerformAction() { return isFrozen; }

    public abstract Actor withFrozen(boolean isFrozon);
    public abstract Set<Specialization> getAllowedTransitions();
    public abstract Set<Action> getAllowedActions();
    public abstract Specialization getCurrentState();
}
