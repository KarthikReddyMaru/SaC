package com.sac.util;

import com.sac.model.actor.Specialization;

public class MessageFormat {

    public static String chooseMessage(String username) {
        return String.format("%s will choose the position now", username);
    }

    public static String spawnSuccessAction(String username, int actionPerformedOn) {
        return String.format("%s placed %s on position %d",
                username, Specialization.NOVICE.name(), actionPerformedOn);
    }

    public static String illegalAction() {
        return "You cannot perform action at this state";
    }

    public static String evolveSuccessAction(String username, int actionPerformedOn,
                                             Specialization from, Specialization to) {
        return String.format("%s evolved position %d from %s to %s",
                username, actionPerformedOn, from, to);
    }
}
