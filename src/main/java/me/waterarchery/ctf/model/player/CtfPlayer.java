package me.waterarchery.ctf.model.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.waterarchery.ctf.model.game.GameLocation;
import me.waterarchery.ctf.model.player.constant.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Setter
@Getter
@RequiredArgsConstructor
public class CtfPlayer {

    private final UUID uuid;
    private final String name;
    private final String gameId;
    private final GameLocation oldLocation; // last location before joining the game
    private final GameTeam team;
    private int captures = 0;
    private int kills = 0;
    private int deaths = 0;

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
