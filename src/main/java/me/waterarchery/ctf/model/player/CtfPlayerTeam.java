package me.waterarchery.ctf.model.player;

import lombok.Getter;
import lombok.Setter;
import me.waterarchery.ctf.model.game.GameLocation;
import me.waterarchery.ctf.model.player.constant.GameTeam;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
public class CtfPlayerTeam {

    private final ConcurrentHashMap<UUID, CtfPlayer> players = new ConcurrentHashMap<>();
    private final GameTeam team;
    private GameLocation flagLocation;
    private GameLocation spawnLocation;
    private int captures = 0;

    public CtfPlayerTeam(GameTeam team, GameLocation flagLocation, GameLocation spawnLocation) {
        this.team = team;
        this.flagLocation = flagLocation;
        this.spawnLocation = spawnLocation;
    }
}
