package me.waterarchery.ctf.manager;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.ConfigFile;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.configuration.SoundsFile;
import me.waterarchery.ctf.database.CtfDatabase;
import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.model.game.GameLocation;
import me.waterarchery.ctf.model.game.constant.GameStatus;
import me.waterarchery.ctf.model.player.CtfPlayer;
import me.waterarchery.ctf.model.player.CtfPlayerTeam;
import me.waterarchery.ctf.model.player.constant.GameTeam;
import me.waterarchery.ctf.util.ChatUtils;
import me.waterarchery.ctf.util.SoundUtils;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GameManager {

    private static GameManager instance;

    private final CaptureTheFlag plugin;
    private final CacheManager cacheManager;
    private final BossBarManager bossBarManager;
    private final ScoreBoardManager scoreBoardManager;
    private final ConfigFile config;
    private final LangFile lang;
    private final SoundsFile sounds;

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }

        return instance;
    }

    private GameManager() {
        plugin = CaptureTheFlag.getInstance();
        cacheManager = CacheManager.getInstance();
        bossBarManager = BossBarManager.getInstance();
        scoreBoardManager = ScoreBoardManager.getInstance();
        config = CaptureTheFlag.getPluginConfig();
        lang = CaptureTheFlag.getLang();
        sounds = CaptureTheFlag.getSounds();
    }

    public void joinGame(Player player, CtfGame ctfGame, GameTeam team) {
        GameLocation oldLocation = new GameLocation(player.getLocation().getWorld().getName(), player.getLocation().getX(),
            player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        CtfPlayer ctfPlayer = new CtfPlayer(player.getUniqueId(), player.getName(), ctfGame.getId(), oldLocation, team);

        cacheManager.getPlayers().put(player.getUniqueId(), ctfPlayer);

        ctfGame.getPlayers().forEach(loopCtfPlayer -> {
            Player bukkitPlayer = loopCtfPlayer.getBukkitPlayer();
            String broadcastMessage = lang.getPlayerJoinedBroadcast().replace("%player-name%", player.getName());
            ChatUtils.sendPrefixedMessage(bukkitPlayer, broadcastMessage);
            SoundUtils.sendSoundRaw(bukkitPlayer, sounds.getPlayerJoined());
        });

        ctfGame.addPlayer(ctfPlayer, team);
        bossBarManager.addBossBar(player, ctfGame);
        scoreBoardManager.createScoreBoard(player, ctfGame, team);

        GameLocation spawnLocation = ctfGame.getSpawnLocation(team);
        player.teleportAsync(spawnLocation.toLocation());
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);

        handlePlayerItemGive(ctfPlayer);
    }

    public void leaveGame(Player player) {
        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        CtfGame ctfGame = cacheManager.getGames().get(ctfPlayer.getGameId());

        ctfGame.removePlayer(player.getUniqueId());
        cacheManager.getPlayers().remove(player.getUniqueId());
        handleExit(player, ctfPlayer.getOldLocation(), ctfGame);

        ctfGame.getPlayers().forEach(loopCtfPlayer -> {
            Player bukkitPlayer = loopCtfPlayer.getBukkitPlayer();
            String message = lang.getPlayerLeftGame().replace("%player-name%", player.getName());
            ChatUtils.sendPrefixedMessage(bukkitPlayer, message);
            SoundUtils.sendSoundRaw(bukkitPlayer, sounds.getPlayerLeftGame());
        });

        CtfDatabase database = CaptureTheFlag.getDatabase();
        database.incrementPlayerStats(ctfPlayer.getUuid(), ctfPlayer.getKills(), ctfPlayer.getDeaths(), ctfPlayer.getCaptures());
    }

    public void startGame(CtfGame ctfGame) {
        ctfGame.setGameStatus(GameStatus.STARTING);

        int warmupTime = config.getGameStartWarmupTime();
        config.getGameWarmupTimeBroadcast().forEach(time -> {
            int delay = warmupTime - time;
            Bukkit.getAsyncScheduler().runDelayed(plugin, (task) -> ctfGame.getPlayers().forEach(ctfPlayer -> {
                Player bukkitPlayer = ctfPlayer.getBukkitPlayer();
                String message = lang.getGameStartingSoon().replace("%remaining-time%", String.valueOf(time));
                bukkitPlayer.sendMessage(ChatUtils.colorize(message));
            }), delay, TimeUnit.SECONDS);
        });

        Bukkit.getOnlinePlayers()
            .stream()
            .filter(player -> ctfGame.getPlayers().stream().map(CtfPlayer::getUuid).toList().contains(player.getUniqueId()))
            .forEach(regularPlayer -> {
                String message = lang.getGameStartingBroadcast()
                    .replace("%remaining-time%", String.valueOf(warmupTime))
                    .replace("%game-id%", ctfGame.getId());
                regularPlayer.sendMessage(ChatUtils.colorize(message));
            });

        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (task) -> handleGameStart(ctfGame), warmupTime * 20L);
    }

    public void endGame(CtfGame ctfGame) {
        ScheduledTask endTask = cacheManager.getEndWithTimeoutTasks().remove(ctfGame.getId());
        if (endTask != null) endTask.cancel();

        ctfGame.setGameStatus(GameStatus.CLEANING);

        CtfDatabase database = CaptureTheFlag.getDatabase();
        int redScore = ctfGame.getRedTeam().getCaptures();
        int blueScore = ctfGame.getBlueTeam().getCaptures();
        database.saveGameLog(ctfGame.getId(), redScore, blueScore, System.currentTimeMillis() - ctfGame.getStartTime());

        List<CtfPlayer> clonePlayers = List.copyOf(ctfGame.getPlayers());
        clonePlayers.forEach(ctfPlayer -> {
            database.incrementPlayerStats(ctfPlayer.getUuid(), ctfPlayer.getKills(), ctfPlayer.getDeaths(), ctfPlayer.getCaptures());
            cacheManager.getPlayers().remove(ctfPlayer.getUuid());
            ctfGame.removePlayer(ctfPlayer.getUuid());

            Player bukkitPlayer = ctfPlayer.getBukkitPlayer();
            String endMessage;
            if (redScore == blueScore) {
                endMessage = lang.getGameEndedTie()
                    .replace("%red-score%", String.valueOf(redScore))
                    .replace("%blue-score%", String.valueOf(blueScore));
            } else {
                String winnerTeam = redScore > blueScore ? lang.getPlaceholders().getRed() : lang.getPlaceholders().getBlue();
                endMessage = lang.getGameEnded()
                    .replace("%winner-team%", winnerTeam)
                    .replace("%red-score%", String.valueOf(redScore))
                    .replace("%blue-score%", String.valueOf(blueScore));
            }
            ChatUtils.sendPrefixedMessage(bukkitPlayer, endMessage);
            SoundUtils.sendSoundRaw(bukkitPlayer, sounds.getGameEnded());
        });

        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (task) -> clonePlayers.forEach(ctfPlayer -> {
            Player bukkitPlayer = ctfPlayer.getBukkitPlayer();
            if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return; // this can return null since the player can quit after the game ends

            handleExit(bukkitPlayer, ctfPlayer.getOldLocation(), ctfGame);

            ctfGame.getRedTeam().setCaptures(0);
            ctfGame.getBlueTeam().setCaptures(0);
            ctfGame.setGameStatus(GameStatus.WAITING_FOR_PLAYERS);
        }), 100);
    }

    public void handleCaptureScore(CtfGame ctfGame, CtfPlayer capturePlayer) {
        Player bukkitPlayer = capturePlayer.getBukkitPlayer();
        Material captureItem = capturePlayer.getTeam() == GameTeam.BLUE ? config.getRedTeamFlag() : config.getBlueTeamFlag();
        bukkitPlayer.getInventory().remove(captureItem);

        CtfPlayerTeam ctfGameTeam = ctfGame.getTeam(capturePlayer.getTeam());
        ctfGameTeam.setCaptures(ctfGameTeam.getCaptures() + 1);
        bossBarManager.updateBossBar(ctfGame);

        int redScore = ctfGame.getRedTeam().getCaptures();
        int blueScore = ctfGame.getBlueTeam().getCaptures();
        String message = lang.getFlagCapturedBroadcast()
            .replace("%player-name%", bukkitPlayer.getName())
            .replace("%red-score%", String.valueOf(redScore))
            .replace("%blue-score%", String.valueOf(blueScore));
        ctfGame.getPlayers().forEach(loopCtfPlayer -> {
            Player loopBukkitPlayer = loopCtfPlayer.getBukkitPlayer();
            ChatUtils.sendPrefixedMessage(loopBukkitPlayer, message);
            SoundUtils.sendSoundRaw(loopBukkitPlayer, sounds.getFlagCaptured());

            scoreBoardManager.updateScoreBoard(loopBukkitPlayer, ctfGame, capturePlayer.getTeam());
        });

        if (ctfGameTeam.getCaptures() >= config.getCapturePerGame()) {
            endGame(ctfGame);
        } else {
            GameTeam enemyTeam = capturePlayer.getTeam() == GameTeam.BLUE ? GameTeam.RED : GameTeam.BLUE;
            resetFlag(ctfGame, enemyTeam);
        }

        GameLocation flagLocation = ctfGame.getTeam(capturePlayer.getTeam()).getFlagLocation();
        Location flagBukkitLocation = flagLocation.toLocation();
        Color color = capturePlayer.getTeam() == GameTeam.RED ? Color.RED : Color.BLUE;
        spawnFirework(flagBukkitLocation, color);
    }

    public void resetFlag(CtfGame ctfGame, GameTeam team) {
        GameLocation flagLocation = ctfGame.getTeam(team).getFlagLocation();
        Location flagBukkitLocation = flagLocation.toLocation();

        Material material = team == GameTeam.BLUE ? config.getBlueTeamFlag() : config.getRedTeamFlag();
        flagBukkitLocation.getBlock().setType(material);
    }

    public boolean isProperlySetup(CtfGame ctfGame) {
        return ctfGame.getRedTeam().getFlagLocation() != null &&
            ctfGame.getRedTeam().getSpawnLocation() != null &&
            ctfGame.getBlueTeam().getFlagLocation() != null &&
            ctfGame.getBlueTeam().getSpawnLocation() != null;
    }

    public void spawnFirework(Location location, Color color) {
        Firework f = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        FireworkEffect fireworkEffect = FireworkEffect.builder()
            .flicker(true)
            .trail(true)
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(color)
            .withFade(Color.BLUE)
            .build();
        fm.addEffect(fireworkEffect);
        fm.setPower(0);
        f.setFireworkMeta(fm);
    }

    private void handleGameStart(CtfGame ctfGame) {
        ctfGame.setStartTime(System.currentTimeMillis());
        ctfGame.setGameStatus(GameStatus.IN_PROGRESS);

        resetFlag(ctfGame, GameTeam.BLUE);
        resetFlag(ctfGame, GameTeam.RED);

        ctfGame.getPlayers().forEach(ctfPlayer -> {
            GameLocation spawnLocation = ctfGame.getSpawnLocation(ctfPlayer.getTeam());

            Player bukkitPlayer = ctfPlayer.getBukkitPlayer();
            bukkitPlayer.teleportAsync(spawnLocation.toLocation());
            bukkitPlayer.sendMessage(ChatUtils.colorize(lang.getGameStarted()));
        });

        ScheduledTask endTask = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (task) -> {
            ctfGame.getPlayers().forEach(ctfPlayer -> {
                Player bukkitPlayer = ctfPlayer.getBukkitPlayer();
                ChatUtils.sendPrefixedMessage(bukkitPlayer, lang.getGameTimeoutReached());
                SoundUtils.sendSoundRaw(bukkitPlayer, sounds.getGameEnded());
            });
            endGame(ctfGame);
        }, config.getGameEndTimeoutInMinutes() * 20 * 60L);
        cacheManager.getEndWithTimeoutTasks().put(ctfGame.getId(), endTask);
    }

    private void handleExit(Player player, GameLocation oldLocation, CtfGame ctfGame) {
        if (player.getGameMode() == GameMode.SPECTATOR) player.setGameMode(GameMode.SURVIVAL);

        Location location = oldLocation.toLocation();
        player.teleport(location); // cant use teleportAsync since it will be also triggered on player quit

        bossBarManager.removeBossBar(player, ctfGame);
        scoreBoardManager.removeScoreBoard(player);
    }

    private void handlePlayerItemGive(CtfPlayer ctfPlayer) {
        int rawColor = ctfPlayer.getTeam() == GameTeam.BLUE ? config.getBlueTeamArmorColor() : config.getRedTeamArmorColor();
        Color color = Color.fromRGB(rawColor);
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        ItemStack sword = new ItemStack(Material.STONE_SWORD);

        helmet = colorizeItem(helmet, color);
        chestplate = colorizeItem(chestplate, color);
        leggings = colorizeItem(leggings, color);
        boots = colorizeItem(boots, color);

        Player bukkitPlayer = ctfPlayer.getBukkitPlayer();
        bukkitPlayer.getInventory().clear();

        bukkitPlayer.getInventory().setArmorContents(new ItemStack[]{boots, leggings, chestplate, helmet});
        bukkitPlayer.getInventory().setItem(0, sword);
    }

    private ItemStack colorizeItem(ItemStack itemStack, Color color) {
        if (itemStack == null) return null;

        LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
        if (meta == null) return null;

        meta.setColor(color);
        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
