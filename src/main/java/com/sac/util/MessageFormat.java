package com.sac.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.GameState;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ServerResponse;
import com.sac.model.message.ServerResponse.Type;
import com.sac.service.GameRendererService;
import com.sac.strategy.action.GameAction;

public class MessageFormat {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SYSTEM = "System";

    private static String createJson(Type type, String message) {
        try {
            return objectMapper.writeValueAsString(new ServerResponse(type, SYSTEM, message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    private static String createJson(Type type, String sender, String message) {
        try {
            return objectMapper.writeValueAsString(new ServerResponse(type, sender, message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    /**
     * System Messages
     */

    public static String systemInfo(String message) {
        return createJson(Type.INFO, message);
    }

    public static String systemError(String message) {
        return createJson(Type.ERROR, message);
    }

    /**
     * Choose Messages
     */

    public static String chooseMessage(String username) {
        String message = String.format("Your turn to choose a position, %s!", username);
        return createJson(Type.INFO, message);
    }

    public static String chosenResponseMessage(String username) {
        String message = String.format("%s has selected a position.", username);
        return createJson(Type.INFO, message);
    }

    /**
     * Success Actions
     */

    public static String spawnSuccessAction(String username, int actionPerformedOn) {
        String message = String.format("%s spawned Novice @ %d", username, actionPerformedOn);
        return createJson(Type.INFO, message);
    }

    public static String evolveSuccessAction(String username, int actionPerformedOn,
                                             Specialization from, Specialization to) {
        String message = String.format("%s: Evolved %s ➔ %s (Pos %d)", username, from, to, actionPerformedOn);
        return createJson(Type.INFO, message);
    }

    public static String kamikazeSuccessAction(String username, String opponent, int opponentPosition) {
        String message = String.format("%s hit position %d, but %s was unaffected",
                username, opponentPosition, opponent);
        return createJson(Type.INFO, message);
    }

    public static String kamikazeSuccessActionWithDegradation(String username, int opponentPositionId,
                                                              Specialization from, Specialization to) {
        String message = String.format("%s hit position %d, degrading %s to %s",
                username, opponentPositionId, from.toString().toLowerCase(), to.toString().toLowerCase());
        return createJson(Type.INFO, message);
    }

    public static String captureSuccessAction(String username, String opponent, int positionId) {
        String message = String.format("%s captured pos %d from %s", username, positionId, opponent);
        return createJson(Type.INFO, message);
    }

    /**
     * Failed Actions
     */

    public static String illegalAction() {
        String message = "Action unavailable";
        return createJson(Type.ERROR, message);
    }

    public static String noDestinationProvided() {
        String message = "Please choose a destination";
        return createJson(Type.ERROR, message);
    }

    public static String noActorPresent(int position) {
        String message = String.format("Cannot perform action: No unit at position %d", position);
        return createJson(Type.ERROR, message);
    }

    public static String capturedTrouble(String opponent, int positionId) {
        String message = String.format("Position %d: Already owned by %s", positionId, opponent);
        return createJson(Type.ERROR, message);
    }

    public static String actorCannotPerform(Specialization specialization, GameAction gameAction) {
        String message = String.format("Incompatible: %s cannot use %s.", specialization, gameAction);
        return createJson(Type.ERROR, message);
    }

    public static String foulMessage(String username) {
        String message = String.format("%s committed a foul! (-1 Score)", username);
        return createJson(Type.ERROR, message);
    }

    /**
     * Game States
     */

    public static String gameState(GameState gameState) {
        String state = GameRendererService.render(gameState);
        return createJson(Type.STATE, state);
    }

    public static String endGameWithWinner(String winner, GameState gameState) {
        String state = GameRendererService.render(gameState);
        return createJson(Type.FINISH, winner, state);
    }
}
