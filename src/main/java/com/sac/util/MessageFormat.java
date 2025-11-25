package com.sac.util;

import com.sac.model.actor.Specialization;
import com.sac.strategy.action.GameAction;

public class MessageFormat {

    public static String chooseMessage(String username) {
        return String.format("%s will choose the position now", username);
    }

    public static String noActorPresent(int position) {
        return String.format("No actor present at %d", position);
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

    public static String frozenTrouble(int positionId, Specialization specialization) {
        return String.format("%s at %d is frozen", specialization, positionId);
    }

    public static String capturedTrouble(String opponent, int positionId) {
        return String.format("%s already captured this position %d", opponent, positionId);
    }

    public static String actorCannotPerform(Specialization specialization, GameAction gameAction) {
        return String.format("%s cannot perform this %s", specialization, gameAction);
    }

    public static String capturePosition(String username, String opponent, int positionId) {
        return String.format("%s captured %s's position - %d", username, opponent, positionId);
    }
}
