package com.sac.model;

import com.sac.factory.ActorFactory;
import com.sac.model.actor.Actor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;

@Getter
@Builder(toBuilder = true)
public class Position {

    private int positionId;
    private boolean isCapturedByOpponent;
    private String belongsTo;
    private Actor actor;

    private final PositionCareTaker positionCareTaker = new PositionCareTaker();

    public void setActor(Actor actor) {
        if (!this.isCapturedByOpponent) {
            positionCareTaker.saveMemento(new PositionMemento(this.actor, this.belongsTo, false));
            this.actor = actor.withFrozen(actor.isFrozen());
        }
    }

    public void capturePosition(String belongsTo, boolean isCapturedByOpponent) {
        if (isCapturedByOpponent) {
            positionCareTaker.saveMemento(new PositionMemento(this.actor, this.belongsTo, this.isCapturedByOpponent));
            this.actor = null;
            this.belongsTo = belongsTo;
            this.isCapturedByOpponent = true;
        }
    }

    public void buildFrom(Position position) {
        positionCareTaker.saveMemento(new PositionMemento(this.actor, this.belongsTo, this.isCapturedByOpponent));
        this.actor = position.getActor().withFrozen(position.getActor().isFrozen());
        this.belongsTo = position.getBelongsTo();
        this.isCapturedByOpponent = position.isCapturedByOpponent;
    }

    public boolean restorePosition() {
        PositionMemento positionMemento = positionCareTaker.restoreMemento();
        if (positionMemento != null) {
            this.actor = positionMemento.actor();
            this.isCapturedByOpponent = positionMemento.isCapturedByOpponent();
            this.belongsTo = positionMemento.belongsTo();
            return true;
        }
        return false;
    }

    private record PositionMemento(Actor actor, String belongsTo, boolean isCapturedByOpponent) {}

    private static class PositionCareTaker {

        private final Deque<PositionMemento> history = new ArrayDeque<>();

        public void saveMemento(PositionMemento positionMemento) {
            if (history.size() == 3)
                history.removeLast();
            history.addFirst(positionMemento);
        }

        public PositionMemento restoreMemento() {
            if (history.isEmpty())
                return null;
            return history.removeFirst();
        }

    }
}
