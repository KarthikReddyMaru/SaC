package com.sac.model.actor;

import com.sac.config.actor.ActorActionConfig;
import com.sac.config.actor.ActorEvolutionConfig;
import com.sac.strategy.action.GameAction;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@SuperBuilder(toBuilder = true)
public class Veteran extends Actor {

    @Override
    public Set<Specialization> getAllowedTransitions() {
        return ActorEvolutionConfig.getEvolutions(getCurrentState());
    }

    @Override
    public Set<GameAction> getAllowedActions() {
        return ActorActionConfig.getAllowedActions(getCurrentState());
    }

    @Override
    public Specialization getCurrentState() {
        return Specialization.VETERAN;
    }

    @Override
    public Actor copy() {
        return this.toBuilder().build();
    }
}
