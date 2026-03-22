package me.waterarchery.ctf.database;

import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.model.game.GameLocation;
import me.waterarchery.ctf.model.game.GameLog;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CtfDatabase {

    void saveGameLog(String gameId, int redScore, int blueScore, long duration);

    void saveGame(String gameId, GameLocation redFlag, GameLocation blueFlag, GameLocation redSpawn, GameLocation blueSpawn);

    void saveGame(CtfGame ctfGame);

    void incrementPlayerStats(UUID uuid, int kills, int deaths, int captures);

    CompletableFuture<List<GameLog>> loadGameLogs();

    CompletableFuture<List<GameLog>> loadGameLogs(String gameId);

    List<CtfGame> loadGames();

    void close();
}
