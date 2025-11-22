package com.sac.model;

import com.sac.model.actor.Actor;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;

@Getter
public class Position {

    private int positionId;
    private boolean isCapturedByOpponent;
    private String belongsTo;
    private Actor actor;

    private final PositionCareTaker positionCareTaker = new PositionCareTaker();

    public void setActor(Actor actor) {
        if (!this.isCapturedByOpponent) {
            Actor clonedActor = this.actor == null ? null : this.actor.clone();
            positionCareTaker.saveMemento(new PositionMemento(clonedActor, this.belongsTo, this.isCapturedByOpponent));
            this.actor = actor;
        }
    }

    public void updatePositionOwnership(String belongsTo, boolean isCapturedByOpponent) {
        if (isCapturedByOpponent) {
            Actor clonedActor = this.actor == null ? null : this.actor.clone();
            positionCareTaker.saveMemento(new PositionMemento(clonedActor, this.belongsTo, this.isCapturedByOpponent));
            this.actor = null;
            this.belongsTo = belongsTo;
            this.isCapturedByOpponent = true;
        }
    }

    public void restorePosition() {
        PositionMemento positionMemento = positionCareTaker.restoreMemento();
        if (positionMemento != null) {
            this.actor = positionMemento.actor();
            this.isCapturedByOpponent = positionMemento.isCapturedByOpponent();
            this.belongsTo = positionMemento.belongsTo();
        }
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
