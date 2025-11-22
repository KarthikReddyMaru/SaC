package com.sac.model.actor;

import com.sac.config.actor.ActorEvolutionConfig;
import com.sac.factory.ActorEvolutionFactory;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Novice implements Actor {

    AtomicInteger cooldown = new AtomicInteger(0);

    @Override
    public Specialization getCurrentState() {
        return Specialization.NOVICE;
    }

    @Override
    public Set<Specialization> getAllowedTransitions() {
        return ActorEvolutionConfig.getEvolutions(getCurrentState());
    }

    @Override
    public Actor evolve(Specialization specialization) throws IllegalStateException {
        return ActorEvolutionFactory.getInstance(specialization);
    }

    @Override
    public boolean canPerformAction() {
        return cooldown.get() == 0;
    }

    @Override
    public void decrementCoolDown() {
        if (cooldown.get() > 0)
            cooldown.decrementAndGet();
    }

    @Override
    public void setCoolDown(int cooldown) {
        this.cooldown.set(cooldown);
    }

    @Override
    public int coolDownLeft() {
        return cooldown.get();
    }
}
