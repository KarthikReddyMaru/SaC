package com.sac.config.actor;

import com.sac.model.actor.Specialization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ActorEvolutionConfig {

    private final static Map<Specialization, Set<Specialization>> evolutions = new HashMap<>();

    static {
        evolutions.put(Specialization.NOVICE,
                Set.of(Specialization.FIGHTER, Specialization.WIZARD, Specialization.HEALER));
        evolutions.put(Specialization.FIGHTER,
                Collections.emptySet());
        evolutions.put(Specialization.HEALER,
                Collections.emptySet());
        evolutions.put(Specialization.WIZARD,
                Collections.emptySet());
    }

    public static Set<Specialization> getEvolutions(String fromSpecialization) {
        Specialization specialization = Specialization.fromString(fromSpecialization);
        return getEvolutions(specialization);
    }

    public static Set<Specialization> getEvolutions(Specialization fromSpecialization) {
        return evolutions.getOrDefault(fromSpecialization, Collections.emptySet());
    }

    public static boolean isEvolutionAllowed(String fromSpecialization, String toSpecialization) {
        return isEvolutionAllowed(
                Specialization.fromString(fromSpecialization),
                Specialization.fromString(toSpecialization)
        );
    }

    public static boolean isEvolutionAllowed(Specialization fromSpecialization, Specialization toSpecialization) {
        return evolutions.getOrDefault(fromSpecialization, Collections.emptySet()).contains(toSpecialization);
    }
}
