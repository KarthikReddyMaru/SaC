package com.sac.service;

import com.sac.model.GameState;
import com.sac.model.Position;
import com.sac.model.message.ServerResponse;
import com.sac.util.MessageFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChosenResponseService {

    private final PointsService pointsService;
    private final GameStateService gameStateService;
    private final MessageService messageService;

    public void processChosenResponses(String roomId, GameState gameState, Map<String, Integer> respondedPlayers) {

        String  chosenPlayerId = gameState.getCurrentPlayerId();
        int chosenPosition = respondedPlayers.get(chosenPlayerId);

        String guessedPlayerId = gameStateService.getOpponentId(roomId, chosenPlayerId);
        int guessedPosition = respondedPlayers.get(guessedPlayerId);

        if (chosenPosition != guessedPosition) {

            gameStateService.setCurrentPlayerId(roomId, chosenPlayerId);
            gameStateService.setActionPendingOn(roomId, chosenPosition);
            messageService.broadcastMessage(
                    String.format("%s won, time for action on position %s", chosenPlayerId, chosenPosition),
                    roomId, ServerResponse.Type.INFO);

            if (!isValidActionPosition(roomId, chosenPlayerId, chosenPosition)) {
                pointsService.foulMove(roomId, chosenPlayerId);
                changeChooseOwnership(roomId, guessedPlayerId);
            }
        } else {
            gameStateService.setCurrentPlayerId(roomId, guessedPlayerId);
            int pointsForOpponent = pointsService.computeGuessPoints(roomId, chosenPosition, chosenPlayerId);
            pointsService.addPoints(roomId, guessedPlayerId, pointsForOpponent);
            messageService.broadcastMessage(
                    String.format("%s won, gear up to choose", guessedPlayerId),
                    roomId, ServerResponse.Type.INFO);
        }
        messageService.broadcastMessage(MessageFormat.gameState(gameState), roomId);
        respondedPlayers.clear();
    }

    public boolean isValidActionPosition(String roomId, String username, int chosenPosition) {
        Position position = gameStateService.getPlayerPosition(roomId, username, chosenPosition);
        if (position.isCapturedByOpponent()) {
            messageService.broadcastMessage(
                    MessageFormat.capturedTrouble(position.getBelongsTo(), position.getPositionId()), roomId);
            return false;
        }
        return true;
    }

    private void changeChooseOwnership(String roomId, String opponentName) {
        GameState gameState = gameStateService.getGameState(roomId);
        gameState.setActionPending(false);
        gameState.setActionPendingOn(null);
        gameState.setCurrentPlayerId(opponentName);
        messageService.broadcastMessage(MessageFormat.chooseMessage(opponentName), roomId);
    }

}
