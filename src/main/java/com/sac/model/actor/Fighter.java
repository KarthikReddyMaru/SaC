package com.sac.model.actor;

import com.sac.config.actor.ActorActionConfig;
import com.sac.config.actor.ActorEvolutionConfig;
import com.sac.strategy.action.Action;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@SuperBuilder(toBuilder = true)
public class Fighter extends Actor {

    @Override
    public Set<Specialization> getAllowedTransitions() {
        return ActorEvolutionConfig.getEvolutions(getCurrentState());
    }

    @Override
    public Set<Action> getAllowedActions() {
        return ActorActionConfig.getAllowedActions(getCurrentState());
    }

    @Override
    public Specialization getCurrentState() {
        return Specialization.FIGHTER;
    }

    @Override
    public Actor withFrozen(boolean isFrozon) {
        return this.toBuilder()
                .isFrozen(isFrozen)
                .build();
    }
}
