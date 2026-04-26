package com.sac.model.actor;

import java.util.Arrays;

public enum Specialization {
    RECRUIT("Base actor", 1),
    VETERAN("Can assault and seize a position in a single move", 2),
    PHANTOM("Can wipe out a single type of actor from opposition board in one go", 2);

    public final String description;
    public final int level;

    Specialization(String description, int level) {
        this.description = description;
        this.level = level;
    }

    public static Specialization fromString(String type) {
        Specialization[] specializations = Specialization.values();
        return Arrays.stream(specializations)
                .filter(specialization -> specialization.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}

