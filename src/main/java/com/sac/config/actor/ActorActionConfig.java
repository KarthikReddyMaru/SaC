package com.sac.config.actor;

import com.sac.strategy.action.Action;
import com.sac.strategy.action.GameAction;
import com.sac.model.actor.Specialization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sac.model.actor.Specialization.FIGHTER;
import static com.sac.model.actor.Specialization.NOVICE;
import static com.sac.strategy.action.GameAction.ATTACK_AND_CAPTURE;
import static com.sac.strategy.action.GameAction.EVOLVE;

public class ActorActionConfig {

    private final static Map<Specialization, Set<GameAction>> actions = new HashMap<>();

    static {
        actions.put(NOVICE,
                Set.of(EVOLVE));
        actions.put(FIGHTER,
                Set.of(ATTACK_AND_CAPTURE));
    }

    public static boolean isActionAllowed(Specialization specialization, GameAction gameAction) {
        return actions.getOrDefault(specialization, Collections.emptySet())
                .stream()
                .anyMatch(action -> action.equals(gameAction));
    }

    public static Set<GameAction> getAllowedActions(Specialization specialization) {
        return actions.getOrDefault(specialization, Collections.emptySet());
    }
}
