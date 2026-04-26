package com.sac.util.mode;

import com.sac.model.GameState;
import com.sac.model.GameState.Player;
import com.sac.model.actor.Actor;

import static com.sac.model.GameState.GameplayStatus.FINISHED;
import static com.sac.model.GameState.State.ACTION_REQUIRED;
import static com.sac.model.GameState.State.ROLL;

public class ClassicPointsUtil {

    /**
     * Changes current player to nextPlayer and resets all the actions pending
     *
     * @param gameState  gameState
     */
    public static void transitionRollToNextPlayer(GameState gameState) {
        Player nextPlayer = gameState.cycleNextPlayer();
        gameState.setState(ROLL);
        gameState.setCurrentPlayerId(nextPlayer.getClientId());
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        gameState.setActionPendingOnActor(null);
    }

    public static void transitionRollToCurrentPlayer(GameState gameState) {
        gameState.setState(ROLL);
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        gameState.setActionPendingOnActor(null);
    }

    public static void requirePlayerAction(String currentPlayer, GameState gameState, int positionId) {
        gameState.setCurrentPlayerId(currentPlayer);
        gameState.setActionPending(true);
        gameState.setActionPendingOn(positionId);
        Actor actor = gameState.getPlayerPosition(currentPlayer, positionId)
                               .getActor();
        gameState.setActionPendingOnActor(actor == null ? null : actor.getCurrentState());
        gameState.setState(ACTION_REQUIRED);
    }

    public static void endGameWithWinner(String winner, GameState gameState) {
        gameState.setGameplayStatus(FINISHED);
        gameState.setWinner(winner);
        gameState.setActionPendingOn(null);
        gameState.setCurrentPlayerId(null);
        gameState.setActionPending(false);
        gameState.setActionPendingOnActor(null);
    }
}
