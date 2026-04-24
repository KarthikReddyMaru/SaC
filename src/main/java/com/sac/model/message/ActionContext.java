package com.sac.model.message;

import com.sac.strategy.action.GameAction;
import com.sac.model.actor.Specialization;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionContext {

    private GameAction gameAction;
    private Integer sourcePosition; // Only for kamikaze for now
    private Integer destinationPosition;
    private Specialization specialization;

}
