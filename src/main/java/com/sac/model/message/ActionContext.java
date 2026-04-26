package com.sac.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sac.model.actor.Specialization;
import com.sac.strategy.action.GameAction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionContext {

    private GameAction gameAction;
    private Integer sourcePosition; // Only for kamikaze for now
    private Integer destinationPosition;
    private String sourcePositionHolder;
    private String destinationPositionHolder;
    private Specialization specialization;
    @JsonIgnore
    private String additionalInfo;

}
