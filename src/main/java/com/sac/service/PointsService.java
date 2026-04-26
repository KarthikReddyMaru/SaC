package com.sac.service;

import com.sac.model.GameState.Player;
import com.sac.model.Position;
import com.sac.model.actor.Actor;
import com.sac.model.actor.Specialization;
import com.sac.util.MessageFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PointsService {

    private final GameStateService gameStateService;
    private final MessageService messageService;

    public void addPoints(String roomId, String playerId, int points) {
        Player player = gameStateService.getGameState(roomId).getPlayer(playerId);
        player.addPoints(points);

        if (points != 0)
            messageService.broadcastMessage(MessageFormat.successPointsMessage(player.getUsername(), points), roomId);
    }

}
