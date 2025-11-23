package com.sac.model.message;

import com.sac.strategy.action.GameAction;
import com.sac.model.actor.Specialization;
import lombok.Data;

@Data
public class ActionContext {

    private GameAction gameAction;
    private int sourcePosition;
    private int destinationPosition;
    private int allyPosition;
    private Specialization specialization;

}
