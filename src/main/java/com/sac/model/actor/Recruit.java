package com.sac.model.actor;

import com.sac.config.actor.ActorActionConfig;
import com.sac.config.actor.ActorEvolutionConfig;
import com.sac.model.GameMode;
import com.sac.strategy.action.GameAction;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@SuperBuilder(toBuilder = true)
public class Recruit extends Actor {

    @Override
    public Specialization getCurrentState() {
        return Specialization.RECRUIT;
    }

    @Override
    public Set<Specialization> getAllowedTransitions(GameMode gameMode) {
        return ActorEvolutionConfig.getEvolutions(getCurrentState(), gameMode);
    }

    @Override
    public Set<GameAction> getAllowedActions(GameMode gameMode) {
        return ActorActionConfig.getAllowedActions(getCurrentState(), gameMode);
    }

    @Override
    public Actor copy() {
        return this.toBuilder().build();
    }
}
