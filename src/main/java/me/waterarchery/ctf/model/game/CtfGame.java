package me.waterarchery.ctf.model.game;

import lombok.Getter;
import lombok.Setter;
import me.waterarchery.ctf.model.game.constant.GameStatus;
import me.waterarchery.ctf.model.player.CtfPlayer;
import me.waterarchery.ctf.model.player.CtfPlayerTeam;
import me.waterarchery.ctf.model.player.constant.GameTeam;

import java.util.*;

@Setter
@Getter
public class CtfGame {

    private final String id;
    private final CtfPlayerTeam blueTeam;
    private final CtfPlayerTeam redTeam;

    private GameStatus gameStatus;
    private long startTime;

    public CtfGame(String id) {
        this.id = id;
        blueTeam = new CtfPlayerTeam(GameTeam.BLUE, null, null);
        redTeam = new CtfPlayerTeam(GameTeam.RED, null, null);
        gameStatus = GameStatus.WAITING_FOR_PLAYERS;
    }

    public CtfGame(String id, GameLocation redFlag, GameLocation blueFlag, GameLocation redSpawn, GameLocation blueSpawn) {
        this.id = id;
        blueTeam = new CtfPlayerTeam(GameTeam.BLUE, blueFlag, blueSpawn);
        redTeam = new CtfPlayerTeam(GameTeam.RED, redFlag, redSpawn);
        gameStatus = GameStatus.WAITING_FOR_PLAYERS;
    }

    public void addPlayer(CtfPlayer player, GameTeam team) {
        CtfPlayerTeam ctfPlayerTeam = team == GameTeam.BLUE ? blueTeam : redTeam;
        ctfPlayerTeam.getPlayers().put(player.getUuid(), player);
    }

    public void removePlayer(UUID uuid) {
        blueTeam.getPlayers().remove(uuid);
        redTeam.getPlayers().remove(uuid);
    }

    public List<CtfPlayer> getPlayers() {
        List<CtfPlayer> players = new ArrayList<>();
        players.addAll(blueTeam.getPlayers().values());
        players.addAll(redTeam.getPlayers().values());
        return Collections.unmodifiableList(players);
    }

    public List<CtfPlayer> getPlayers(GameTeam team) {
        CtfPlayerTeam ctfPlayerTeam = team == GameTeam.BLUE ? blueTeam : redTeam;
        return ctfPlayerTeam.getPlayers().values().stream().toList();
    }

    public CtfPlayer getPlayer(UUID uuid) {
        return getPlayers().stream()
            .filter(player -> player.getUuid().equals(uuid))
            .findFirst()
            .orElse(null);
    }

    public CtfPlayerTeam getTeam(GameTeam team) {
        return team == GameTeam.BLUE ? blueTeam : redTeam;
    }

    public GameLocation getSpawnLocation(GameTeam team) {
        return team == GameTeam.BLUE ? blueTeam.getSpawnLocation() : redTeam.getSpawnLocation();
    }
}
