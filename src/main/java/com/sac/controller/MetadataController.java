package com.sac.controller;

import com.sac.service.RoomConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/online")
@RequiredArgsConstructor
public class MetadataController {

    private final RoomConnectionService roomConnectionService;

    @GetMapping("/players")
    public int totalPlayersOnline() {
        return roomConnectionService.getUserRegistry().size();
    }

}
