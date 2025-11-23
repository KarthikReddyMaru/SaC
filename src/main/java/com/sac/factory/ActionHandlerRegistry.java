package com.sac.factory;

import com.sac.strategy.action.Action;
import com.sac.strategy.action.GameAction;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public final class ActionHandlerRegistry {

    private final Map<GameAction, Action> actionMap = new HashMap<>();

    public ActionHandlerRegistry(List<Action> actions) {
        for (Action action : actions) {
            actionMap.put(action.getActionType(), action);
        }
    }

    public Action getInstance(String gameAction) {
        GameAction action = GameAction.fromString(gameAction);
        return getInstance(action);
    }

    public Action getInstance(GameAction gameAction) {
        if (!actionMap.containsKey(gameAction))
            throw new IllegalArgumentException(String.format("Invalid action type - %s", gameAction.name()));
        return actionMap.get(gameAction);
    }
}
