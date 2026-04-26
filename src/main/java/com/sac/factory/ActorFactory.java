package com.sac.factory;

import com.sac.model.actor.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ActorFactory {
    private static final Map<Specialization, Supplier<Actor>> actors = new HashMap<>();

    static {
        actors.put(Specialization.RECRUIT, () -> Recruit.builder().build());
        actors.put(Specialization.VETERAN, () -> Veteran.builder().build());
        actors.put(Specialization.PHANTOM, () -> Phantom.builder().build());
    }

    public static Actor getInstance(Specialization specialization) {
        if (!actors.containsKey(specialization))
            throw new IllegalArgumentException(String.format("Invalid actor type - %s", specialization.name()));
        return actors.get(specialization).get();
    }
}
