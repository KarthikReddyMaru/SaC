package com.sac.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sac.model.GameState;
import com.sac.model.actor.Specialization;
import com.sac.model.message.ServerResponse;
import com.sac.service.GameRendererService;
import com.sac.strategy.action.GameAction;

public class MessageFormat {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String systemInfo(String message) {
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.INFO, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String systemError(String message) {
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.ERROR, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String chooseMessage(String username) {
        String message = String.format("%s will choose the position now", username);
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.INFO, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String noActorPresent(int position) {
        String message = String.format("No actor present at %d", position);
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.INFO, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String spawnSuccessAction(String username, int actionPerformedOn) {
        String message = String.format("%s placed %s on position %d",
                username, Specialization.NOVICE.name(), actionPerformedOn);
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.INFO, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String illegalAction() {
        String message = "You cannot perform action at this state";
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.ERROR, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String evolveSuccessAction(String username, int actionPerformedOn,
                                             Specialization from, Specialization to) {
        String message = String.format("%s evolved position %d from %s to %s",
                username, actionPerformedOn, from, to);
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.INFO, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String frozenTrouble(int positionId, Specialization specialization) {
        String message = String.format("%s at %d is frozen", specialization, positionId);
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.ERROR, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String capturedTrouble(String opponent, int positionId) {
        String message = String.format("%s already captured this position %d", opponent, positionId);
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.ERROR, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String actorCannotPerform(Specialization specialization, GameAction gameAction) {
        String message = String.format("%s cannot perform this %s", specialization, gameAction);
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.ERROR, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String capturePosition(String username, String opponent, int positionId) {
        String message = String.format("%s captured %s's position - %d", username, opponent, positionId);
        try {
            return objectMapper.writeValueAsString(new ServerResponse(ServerResponse.Type.INFO, "System", message));
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering message\"}";
        }
    }

    public static String gameState(GameState gameState) {
        try {
            String state = GameRendererService.render(gameState);
            ServerResponse response = new ServerResponse(ServerResponse.Type.STATE, "System", state);
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering board\"}";
        }
    }

    public static String endGameWithWinner(String winner, GameState gameState) {
        try {
            String state = GameRendererService.render(gameState);
            ServerResponse response = new ServerResponse(ServerResponse.Type.FINISH, winner, state);
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"sender\":\"System\",\"content\":\"Error rendering response\"}";
        }
    }
}
