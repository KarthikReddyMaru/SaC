package com.sac.model.actor;

import java.util.Set;

public interface Actor {

    void setCoolDown(int coolDownSteps);
    void decrementCoolDown();
    boolean canPerformAction();
    int coolDownLeft();
    Actor evolve(Specialization specialization) throws IllegalStateException;
    Set<Specialization> getAllowedTransitions();
    Specialization getCurrentState();
    Actor clone();
}
