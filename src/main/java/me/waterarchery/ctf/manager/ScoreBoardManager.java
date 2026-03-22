package me.waterarchery.ctf.manager;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.ConfigFile;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.model.game.constant.GameStatus;
import me.waterarchery.ctf.model.player.constant.GameTeam;
import me.waterarchery.ctf.util.ChatUtils;
import me.waterarchery.ctf.util.LangUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;

public class ScoreBoardManager {

    private static final String OBJECTIVE_NAME = "ctf_sidebar";
    private static ScoreBoardManager instance;

    private final CaptureTheFlag plugin;
    private final CacheManager cacheManager;
    private final ConfigFile config;
    private final LangFile lang;

    public static ScoreBoardManager getInstance() {
        if (instance == null) {
            instance = new ScoreBoardManager();
        }
        return instance;
    }

    private ScoreBoardManager() {
        plugin = CaptureTheFlag.getInstance();
        cacheManager = CacheManager.getInstance();
        config = CaptureTheFlag.getPluginConfig();
        lang = CaptureTheFlag.getLang();
    }

    public void createScoreBoard(Player player, CtfGame ctfGame, GameTeam gameTeam) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, buildTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        setLines(board, objective, player, ctfGame, gameTeam);
        player.setScoreboard(board);

        ScheduledTask scoreBoardUpdateTask = Bukkit.getGlobalRegionScheduler()
            .runAtFixedRate(plugin, (task) -> updateScoreBoard(player, ctfGame, gameTeam),
                config.getScoreBoardUpdateInTicks(),
                config.getScoreBoardUpdateInTicks());
        cacheManager.getScoreBoardUpdateTasks().put(player.getUniqueId(), scoreBoardUpdateTask);
    }

    public void updateScoreBoard(Player player, CtfGame ctfGame, GameTeam gameTeam) {
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective(OBJECTIVE_NAME);

        if (objective == null) {
            createScoreBoard(player, ctfGame, gameTeam);
            return;
        }

        objective.displayName(buildTitle());
        clearLines(board);
        setLines(board, objective, player, ctfGame, gameTeam);
    }

    public void removeScoreBoard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective(OBJECTIVE_NAME);
        if (objective != null) {
            objective.unregister();
        }

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        ScheduledTask task = cacheManager.getScoreBoardUpdateTasks().remove(player.getUniqueId());
        task.cancel();
    }

    private Component buildTitle() {
        LangFile.ScoreBoardConfiguration config = lang.getScoreBoard();
        return ChatUtils.colorize(config.getScoreBoardTitle());
    }

    private void setLines(Scoreboard board, Objective objective, Player player, CtfGame ctfGame, GameTeam gameTeam) {
        LangFile.ScoreBoardConfiguration config = lang.getScoreBoard();

        List<String> lines = config.getScoreBoardLines();
        long startTime = ctfGame.getGameStatus() != GameStatus.IN_PROGRESS ? System.currentTimeMillis() : ctfGame.getStartTime();
        String parsedTime = LangUtils.parseTime(System.currentTimeMillis() - startTime);
        String teamPlaceholder = gameTeam == GameTeam.BLUE ? lang.getPlaceholders().getBlue() : lang.getPlaceholders().getRed();
        String gameStatePlaceholder = LangUtils.parseGameStatus(ctfGame.getGameStatus());

        for (int i = 0; i < lines.size(); i++) {
            int score = lines.size() - i;
            String entry = getUniqueEntry(i);

            String line = lines.get(i)
                .replace("%player-name%", player.getName())
                .replace("%game-id%", ctfGame.getId())
                .replace("%team%", teamPlaceholder)
                .replace("%game-state%", gameStatePlaceholder)
                .replace("%game-time%", parsedTime)
                .replace("%red-score%", String.valueOf(ctfGame.getRedTeam().getCaptures()))
                .replace("%blue-score%", String.valueOf(ctfGame.getBlueTeam().getCaptures()));

            Team team = board.registerNewTeam(OBJECTIVE_NAME + "_" + i);
            team.addEntry(entry);
            team.prefix(ChatUtils.colorize(line));

            objective.getScore(entry).setScore(score);
        }
    }

    private void clearLines(Scoreboard board) {
        for (Team team : board.getTeams()) {
            if (team.getName().startsWith(OBJECTIVE_NAME + "_")) {
                team.unregister();
            }
        }

        board.getEntries().forEach(board::resetScores);
    }

    private String getUniqueEntry(int index) {
        return "§" + Integer.toHexString(index);
    }
}