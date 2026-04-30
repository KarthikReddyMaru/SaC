package com.sac.config.actor;

import com.sac.model.GameMode;
import com.sac.model.actor.Specialization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sac.model.GameMode.CLASSIC_POINTS;
import static com.sac.model.actor.Specialization.*;

public final class ActorEvolutionConfig {

    private final static Map<GameMode, Map<Specialization, Set<Specialization>>> evolutions = new HashMap<>();

    static {
        evolutions.put(CLASSIC_POINTS, Map.of(
                RECRUIT, Set.of(VETERAN, PHANTOM),
                VETERAN, Collections.emptySet(),
                PHANTOM, Collections.emptySet()));
    }

    public static Set<Specialization> getEvolutions(Specialization fromSpecialization, GameMode gameMode) {
        return evolutions.get(gameMode).getOrDefault(fromSpecialization, Collections.emptySet());
    }

    public static boolean isActorPresentInMode(GameMode gameMode, Specialization specialization) {
        return evolutions.get(gameMode).containsKey(specialization);
    }

}
