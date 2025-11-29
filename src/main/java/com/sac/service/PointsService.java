package com.sac.service;

import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PointsService {

    private final GameStateService gameStateService;
    private final Map<Specialization, Float> bonusMultiplier = Map.of(
            Specialization.NOVICE, 1f,
            Specialization.FIGHTER, 1.5f);

    public int computeGuessPoints(String roomId, int guessedPosition, String playerId) {
        Player player = gameStateService.getPlayer(roomId, playerId);
        Position position = player.getPositions()[guessedPosition];
        Actor actorInCapturedPosition = position.getActor();
        if (actorInCapturedPosition != null) {
            Specialization capturedActorType = actorInCapturedPosition.getCurrentState();
            int totalActorsOfCapturedType = getCountOfActors(player.getPositions(), capturedActorType);
            return (int) Math.ceil((float) totalActorsOfCapturedType * bonusMultiplier.get(capturedActorType));
        }
        return 1;
    }

    public void addPoints(String roomId, String username, int points) {
        gameStateService.getPlayer(roomId, username)
                .addPoints(points);
    }

    public void foulMove(String roomId, String username) {
        Player player = gameStateService.getPlayer(roomId, username);
        int points = Math.max(player.getPoints() - 1, 0);
        player.setPoints(points);
    }

    private int getCountOfActors(Position[] positions, Specialization specialization) {
        return Math.toIntExact(Arrays.stream(positions)
                .map(Position::getActor)
                .filter(Objects::nonNull)
                .filter(actor -> actor.getCurrentState().equals(specialization))
                .count());
    }

}
