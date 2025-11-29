package com.sac.model.actor;

import java.util.Arrays;

public enum Specialization {
    NOVICE("Base actor", 1),
    FIGHTER("Can assault and seize a position in a single move", 2),
    WIZARD("Can exchange one character between allied and enemy forces", 2),
    HEALER("Can revert one position to its previous state", 2);

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

