package com.sac.config.actor;

import com.sac.strategy.action.Action;
import com.sac.strategy.action.GameAction;
import com.sac.model.actor.Specialization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActorActionConfig {

    private final static Map<Specialization, Set<Action>> actions = new HashMap<>();

    static {
        actions.put(Specialization.NOVICE,
                Collections.emptySet());
    }

    public static boolean isActionAllowed(Specialization specialization, GameAction gameAction) {
        return actions.getOrDefault(specialization, Collections.emptySet())
                .stream()
                .anyMatch(action -> action.getActionType().equals(gameAction));
    }

    public static Set<Action> getAllowedActions(Specialization specialization) {
        return actions.getOrDefault(specialization, Collections.emptySet());
    }
}
