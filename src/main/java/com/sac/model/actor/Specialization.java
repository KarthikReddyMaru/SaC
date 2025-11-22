package com.sac.model.actor;

import java.util.Arrays;

public enum Specialization {
    NOVICE("Base actor"),
    FIGHTER("Can assault and seize a position in a single move"),
    WIZARD("Can exchange one character between allied and enemy forces"),
    HEALER("Can revert one position to its previous state");

    final String description;

    Specialization(String description) {
        this.description = description;
    }

    public static Specialization fromString(String type) {
        Specialization[] specializations = Specialization.values();
        return Arrays.stream(specializations)
                .filter(specialization -> specialization.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}

