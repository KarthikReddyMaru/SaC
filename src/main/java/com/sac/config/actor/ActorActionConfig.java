package com.sac.config.actor;

import com.sac.model.GameMode;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import com.sac.strategy.action.GameAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sac.model.GameMode.CLASSIC_POINTS;
import static com.sac.model.actor.Specialization.*;
import static com.sac.strategy.action.GameAction.*;

public class ActorActionConfig {

    private final static Map<GameMode, Map<Specialization, Set<GameAction>>> actions = new HashMap<>();

    static {
        actions.put(CLASSIC_POINTS, Map.of(
                RECRUIT, Set.of(PROMOTE, REVERT),
                VETERAN, Set.of(CAPTURE),
                PHANTOM, Set.of(BLACKOUT)));
    }

    public static Set<GameAction> getAllowedActions(Specialization specialization, GameMode gameMode) {
        return actions.get(gameMode).getOrDefault(specialization, Collections.emptySet());
    }
}
