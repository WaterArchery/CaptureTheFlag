package me.waterarchery.ctf.manager;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.model.player.CtfPlayer;
import net.kyori.adventure.bossbar.BossBar;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CacheManager {

    private static CacheManager instance;

    private final ConcurrentHashMap<String, CtfGame> games = new ConcurrentHashMap<>(); // gameid -> game
    private final ConcurrentHashMap<UUID, CtfPlayer> players = new ConcurrentHashMap<>(); // player uuid -> player
    private final ConcurrentHashMap<String, BossBar> bossbars = new ConcurrentHashMap<>(); // gameid -> bossbar
    private final ConcurrentHashMap<UUID, ScheduledTask> scoreBoardUpdateTasks = new ConcurrentHashMap<>(); // player -> task
    private final ConcurrentHashMap<String, ScheduledTask> endWithTimeoutTasks = new ConcurrentHashMap<>(); // gameid -> task

    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }

        return instance;
    }

    private CacheManager() {

    }
}
