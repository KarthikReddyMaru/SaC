package com.sac.util;

import com.sac.model.actor.Specialization;

public class MessageFormat {
    public static String chooseMessage(String username) {
        return String.format("%s will choose the position now", username);
    }
    public static String spawnSuccessAction(String username, int actionPendingOn) {
        return String.format("%s placed %s on position %d",
                username, Specialization.NOVICE.name(), actionPendingOn);
    }
}
