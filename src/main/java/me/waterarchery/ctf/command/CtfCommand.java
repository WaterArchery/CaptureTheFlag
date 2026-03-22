package me.waterarchery.ctf.command;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Optional;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.configuration.SoundsFile;
import me.waterarchery.ctf.database.CtfDatabase;
import me.waterarchery.ctf.manager.CacheManager;
import me.waterarchery.ctf.manager.GameManager;
import me.waterarchery.ctf.manager.LoadManager;
import me.waterarchery.ctf.model.command.BaseCommand;
import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.model.game.GameLocation;
import me.waterarchery.ctf.model.game.constant.GameStatus;
import me.waterarchery.ctf.model.game.constant.PositionType;
import me.waterarchery.ctf.model.player.CtfPlayer;
import me.waterarchery.ctf.model.player.constant.GameTeam;
import me.waterarchery.ctf.util.ChatUtils;
import me.waterarchery.ctf.util.LangUtils;
import me.waterarchery.ctf.util.SoundUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
@Command(value = "ctf", alias = {"capturetheflag"})
public class CtfCommand extends BaseCommand {

    private final CacheManager cacheManager;
    private final GameManager gameManager;
    private final LangFile lang;
    private final SoundsFile sounds;

    public CtfCommand() {
        this.cacheManager = CacheManager.getInstance();
        this.gameManager = GameManager.getInstance();
        this.lang = CaptureTheFlag.getLang();
        this.sounds = CaptureTheFlag.getSounds();
    }

    @Command
    public void defaultCommand(CommandSender sender) {
        int totalGames = cacheManager.getGames().size();
        int joinableGames = cacheManager.getGames()
            .values()
            .stream()
            .filter(game -> game.getGameStatus() == GameStatus.STARTING || game.getGameStatus() == GameStatus.IN_PROGRESS)
            .toList()
            .size();

        lang.getMainCommandMessages().forEach(message -> {
            message = message.replace("%total-games%", String.valueOf(totalGames));
            message = message.replace("%joinable-games%", String.valueOf(joinableGames));
            sender.sendMessage(ChatUtils.colorize(message));
        });
    }

    @Command("join")
    public void joinCommand(Player player, @Suggestion("ctf-games") String gameId, @Optional @Suggestion("ctf-teams") GameTeam team) {
        CtfGame ctfGame = cacheManager.getGames().get(gameId);
        if (ctfGame == null) {
            String message = lang.getGameNotFound().replace("%game-id%", gameId);
            ChatUtils.sendPrefixedMessage(player, message);
            SoundUtils.sendSoundRaw(player, sounds.getGameNotFound());
            return;
        }

        if (!gameManager.isProperlySetup(ctfGame)) {
            String message = lang.getGameNotProperlySetup().replace("%game-id%", gameId);
            ChatUtils.sendPrefixedMessage(player, message);
            SoundUtils.sendSoundRaw(player, sounds.getGameNotProperlySetup());
            return;
        }

        if (ctfGame.getGameStatus() != GameStatus.STARTING && ctfGame.getGameStatus() != GameStatus.WAITING_FOR_PLAYERS) {
            String message = lang.getGameIsRunning().replace("%game-id%", gameId);
            ChatUtils.sendPrefixedMessage(player, message);
            SoundUtils.sendSoundRaw(player, sounds.getGameIsRunning());
            return;
        }

        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (ctfPlayer != null) return;

        GameManager gameManager = GameManager.getInstance();
        gameManager.joinGame(player, ctfGame, team);

        String selfMessage = lang.getPlayerJoinedSelf().replace("%game-id%", gameId);
        ChatUtils.sendPrefixedMessage(player, selfMessage);
    }

    @Command("leave")
    public void leaveCommand(Player player) {
        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (ctfPlayer == null) {
            ChatUtils.sendPrefixedMessage(player, lang.getNotInGame());
            SoundUtils.sendSoundRaw(player, sounds.getNotInGame());
            return;
        }

        GameManager gameManager = GameManager.getInstance();
        gameManager.leaveGame(player);

        ChatUtils.sendPrefixedMessage(player, lang.getPlayerLeftSelf());
        SoundUtils.sendSoundRaw(player, sounds.getPlayerLeftSelf());
    }

    @Command("score")
    public void scoreCommand(Player player) {
        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (ctfPlayer == null) {
            ChatUtils.sendPrefixedMessage(player, lang.getNotInGame());
            SoundUtils.sendSoundRaw(player, sounds.getNotInGame());
            return;
        }

        CtfGame ctfGame = cacheManager.getGames().get(ctfPlayer.getGameId());
        if (ctfGame == null) {
            handleGameNull(player, ctfPlayer.getGameId());
            return;
        }

        String message = lang.getScoreMessage()
            .replace("%red-score%", String.valueOf(ctfGame.getRedTeam().getCaptures()))
            .replace("%blue-score%", String.valueOf(ctfGame.getBlueTeam().getCaptures()));
        ChatUtils.sendPrefixedMessage(player, message);
        SoundUtils.sendSoundRaw(player, sounds.getDingDing());
    }

    @Command("reload")
    @Permission({"ctf.admin.*", "ctf.admin.reload"})
    public void reloadCommand(Player player) {
        LoadManager.getInstance().reload();
        ChatUtils.sendPrefixedMessage(player, lang.getConfigReloaded());
        SoundUtils.sendSoundRaw(player, sounds.getConfigReloaded());
    }

    @Command("create")
    @Permission({"ctf.admin.*", "ctf.admin.create"})
    public void createCommand(Player player, String gameId) {
        CtfGame ctfGame = cacheManager.getGames().get(gameId);
        if (ctfGame != null) {
            String message = lang.getGameAlreadyExists().replace("%game-id%", gameId);
            ChatUtils.sendPrefixedMessage(player, message);
            SoundUtils.sendSoundRaw(player, sounds.getGameAlreadyExists());
            return;
        }

        CtfGame newCtfGame = new CtfGame(gameId);
        cacheManager.getGames().put(gameId, newCtfGame);

        String message = lang.getGameCreated().replace("%game-id%", gameId);
        ChatUtils.sendPrefixedMessage(player, message);
        SoundUtils.sendSoundRaw(player, sounds.getGameCreated());
    }

    @Command("start")
    @Permission({"ctf.admin.*", "ctf.admin.start"})
    public void startCommand(Player player, @Suggestion("ctf-games") String gameId) {
        CtfGame ctfGame = cacheManager.getGames().get(gameId);
        if (ctfGame == null) {
            handleGameNull(player, gameId);
            return;
        }

        int redTeamPlayers = ctfGame.getPlayers(GameTeam.RED).size();
        int blueTeamPlayers = ctfGame.getPlayers(GameTeam.BLUE).size();
        if (redTeamPlayers == 0 || blueTeamPlayers == 0) {
            ChatUtils.sendPrefixedMessage(player, lang.getNotEnoughPlayers());
            SoundUtils.sendSoundRaw(player, sounds.getNotEnoughPlayers());
            return;
        }

        if (ctfGame.getGameStatus() == GameStatus.CLEANING) {
            ChatUtils.sendPrefixedMessage(player, lang.getGameIsCleaning().replace("%game-id%", gameId));
            SoundUtils.sendSoundRaw(player, sounds.getGameIsCleaning());
            return;
        }

        if (ctfGame.getGameStatus() == GameStatus.STARTING || ctfGame.getGameStatus() == GameStatus.IN_PROGRESS) {
            ChatUtils.sendPrefixedMessage(player, lang.getGameAlreadyStarting().replace("%game-id%", gameId));
            SoundUtils.sendSoundRaw(player, sounds.getGameAlreadyStarting());
            return;
        }

        GameManager gameManager = GameManager.getInstance();
        gameManager.startGame(ctfGame);
    }

    @Command("stop")
    @Permission({"ctf.admin.*", "ctf.admin.stop"})
    public void stopCommand(Player player, @Suggestion("ctf-games") String gameId) {
        CtfGame ctfGame = cacheManager.getGames().get(gameId);
        if (ctfGame == null) {
            handleGameNull(player, gameId);
            return;
        }

        if (ctfGame.getGameStatus() != GameStatus.IN_PROGRESS) {
            String message = lang.getGameNotInProgress().replace("%game-id%", gameId);
            ChatUtils.sendPrefixedMessage(player, message);
            SoundUtils.sendSoundRaw(player, sounds.getGameNotInProgress());
            return;
        }

        GameManager gameManager = GameManager.getInstance();
        gameManager.endGame(ctfGame);

        String stoppedMessage = lang.getGameStopped().replace("%game-id%", gameId);
        ChatUtils.sendPrefixedMessage(player, stoppedMessage);
        SoundUtils.sendSoundRaw(player, sounds.getGameStopped());
    }

    @Command("set-flag")
    @Permission({"ctf.admin.*", "ctf.admin.setflag"})
    public void setFlagCommand(Player player, @Suggestion("ctf-games") String gameId, @Suggestion("ctf-teams") GameTeam team) {
        Location location = player.getLocation().toBlockLocation();
        location = location.subtract(0, 1, 0);
        handleLocationSet(player, location, gameId, team, PositionType.FLAG);
    }

    @Command("set-location")
    @Permission({"ctf.admin.*", "ctf.admin.setlocation"})
    public void setLocationCommand(Player player, @Suggestion("ctf-games") String gameId, @Suggestion("ctf-teams") GameTeam team) {
        handleLocationSet(player, player.getLocation(), gameId, team, PositionType.SPAWN_POINT);
    }

    @Command("info")
    @Permission({"ctf.admin.*", "ctf.admin.info"})
    public void info(Player player, @Suggestion("ctf-games") String gameId) {
        CtfGame game = cacheManager.getGames().get(gameId);
        if (game == null) {
            handleGameNull(player, gameId);
            return;
        }

        LangFile.PlaceholderConfiguration placeholders = lang.getPlaceholders();
        GameLocation redSpawn = game.getRedTeam().getSpawnLocation();
        GameLocation blueSpawn = game.getBlueTeam().getSpawnLocation();
        GameLocation redFlag = game.getRedTeam().getFlagLocation();
        GameLocation blueFlag = game.getBlueTeam().getFlagLocation();

        lang.getGameInfoMessages().forEach(line -> {
            String formatted = line.replace("%game-id%", gameId)
                .replace("%game-status%", LangUtils.parseGameStatus(game.getGameStatus()))
                .replace("%total-players%", String.valueOf(game.getPlayers().size()))
                .replace("%red-players%", String.valueOf(game.getPlayers(GameTeam.RED).size()))
                .replace("%blue-players%", String.valueOf(game.getPlayers(GameTeam.BLUE).size()))
                .replace("%red-score%", String.valueOf(game.getRedTeam().getCaptures()))
                .replace("%blue-score%", String.valueOf(game.getBlueTeam().getCaptures()))
                .replace("%red-spawn-status%", redSpawn == null ? placeholders.getNotSet() : placeholders.getSet())
                .replace("%blue-spawn-status%", blueSpawn == null ? placeholders.getNotSet() : placeholders.getSet())
                .replace("%red-flag-status%", redFlag == null ? placeholders.getNotSet() : placeholders.getSet())
                .replace("%blue-flag-status%", blueFlag == null ? placeholders.getNotSet() : placeholders.getSet());
            player.sendMessage(ChatUtils.colorize(formatted));
        });

        SoundUtils.sendSoundRaw(player, sounds.getHelpMessage());
    }

    private void handleLocationSet(Player player, Location location, String gameId, GameTeam team, PositionType positionType) {
        CtfGame game = cacheManager.getGames().get(gameId);
        if (game == null) {
            handleGameNull(player, gameId);
            return;
        }

        GameLocation gameLocation = new GameLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        if (positionType == PositionType.FLAG) game.getTeam(team).setFlagLocation(gameLocation);
        else game.getTeam(team).setSpawnLocation(gameLocation);

        CtfDatabase ctfDatabase = CaptureTheFlag.getDatabase();
        ctfDatabase.saveGame(game);

        String message = lang.getLocationSet()
            .replace("%team%", team.name())
            .replace("%game-id%", gameId);
        ChatUtils.sendPrefixedMessage(player, message);
        SoundUtils.sendSound(player, sounds.getLocationSet());
    }

    private void handleGameNull(Player player, String gameId) {
        String message = lang.getGameNotFound().replace("%game-id%", gameId);
        ChatUtils.sendPrefixedMessage(player, message);
        SoundUtils.sendSoundRaw(player, sounds.getGameNotFound());
    }
}
