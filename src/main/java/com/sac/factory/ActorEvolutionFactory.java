package com.sac.factory;

import com.sac.model.actor.Actor;
import com.sac.model.actor.Novice;
import com.sac.model.actor.Specialization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ActorEvolutionFactory {
    private static final Map<Specialization, Supplier<Actor>> actors = new HashMap<>();

    static {
        actors.put(Specialization.NOVICE, Novice::new);
    }

    public static Actor getInstance(Specialization specialization) {
        if (!actors.containsKey(specialization))
            throw new IllegalArgumentException("Invalid specialization "+ specialization);
        return actors.get(specialization).get();
    }
}
