package com.sac.model.actor;

import com.sac.config.actor.ActorActionConfig;
import com.sac.config.actor.ActorEvolutionConfig;
import com.sac.strategy.action.Action;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@SuperBuilder(toBuilder = true)
public class Novice extends Actor {


    @Override
    public Specialization getCurrentState() {
        return Specialization.NOVICE;
    }

    @Override
    public Set<Specialization> getAllowedTransitions() {
        return ActorEvolutionConfig.getEvolutions(getCurrentState());
    }

    @Override
    public Set<Action> getAllowedActions() {
        return ActorActionConfig.getAllowedActions(getCurrentState());
    }

    @Override
    public Actor withCoolDown(int coolDownSteps) {
        return this.toBuilder()
                .cooldown(coolDownSteps)
                .build();
    }

    @Override
    public Actor withDecrementedCoolDown() {
        if (cooldown > 0)
            return this.toBuilder()
                .cooldown(cooldown - 1).build();
        return this;
    }


}
