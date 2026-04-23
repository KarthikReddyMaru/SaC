package com.sac.model.actor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sac.strategy.action.Action;
import com.sac.strategy.action.GameAction;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@SuperBuilder(toBuilder = true)
public abstract class Actor {

    @JsonIgnore
    public abstract Set<Specialization> getAllowedTransitions();
    @JsonIgnore
    public abstract Set<GameAction> getAllowedActions();
    public abstract Specialization getCurrentState();
    public abstract Actor copy();
}
