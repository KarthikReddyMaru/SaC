package com.sac.config.actor;

import com.sac.model.actor.Specialization;
import com.sac.strategy.action.GameAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sac.model.actor.Specialization.FIGHTER;
import static com.sac.model.actor.Specialization.NOVICE;
import static com.sac.strategy.action.GameAction.*;

public class ActorActionConfig {

    private final static Map<Specialization, Set<GameAction>> actions = new HashMap<>();

    static {
        actions.put(NOVICE,
                Set.of(EVOLVE, KAMIKAZE));
        actions.put(FIGHTER,
                Set.of(ATTACK_AND_CAPTURE));
    }

    public static Set<GameAction> getAllowedActions(Specialization specialization) {
        return actions.getOrDefault(specialization, Collections.emptySet());
    }
}
