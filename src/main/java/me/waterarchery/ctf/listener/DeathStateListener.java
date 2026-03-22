package me.waterarchery.ctf.listener;

import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.ConfigFile;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.configuration.SoundsFile;
import me.waterarchery.ctf.manager.CacheManager;
import me.waterarchery.ctf.manager.GameManager;
import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.model.game.GameLocation;
import me.waterarchery.ctf.model.game.constant.GameStatus;
import me.waterarchery.ctf.model.player.CtfPlayer;
import me.waterarchery.ctf.model.player.constant.GameTeam;
import me.waterarchery.ctf.util.ChatUtils;
import me.waterarchery.ctf.util.SoundUtils;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class DeathStateListener implements Listener {

    private final CacheManager cacheManager = CacheManager.getInstance();
    private final GameManager gameManager = GameManager.getInstance();
    private final ConfigFile config = CaptureTheFlag.getPluginConfig();
    private final LangFile lang = CaptureTheFlag.getLang();
    private final SoundsFile sounds = CaptureTheFlag.getSounds();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCtfPlayerDeath(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player victim) {
            if (victim.getHealth() > event.getDamage()) return;
            event.setCancelled(true);

            CtfPlayer killerCtfPlayer = cacheManager.getPlayers().get(damager.getUniqueId());
            CtfPlayer victimCtfPlayer = cacheManager.getPlayers().get(victim.getUniqueId());
            if (killerCtfPlayer == null || victimCtfPlayer == null) return;
            handleStats(killerCtfPlayer, victimCtfPlayer);

            ChatUtils.sendPrefixedMessage(victim, lang.getDeathRespawnMessage());
            Title deathTitle = Title.title(ChatUtils.colorize(lang.getDeathRespawnTitle()),
                ChatUtils.colorize(lang.getDeathRespawnSubtitle()), 5, 40, 5);
            victim.showTitle(deathTitle);
            SoundUtils.sendSoundRaw(victim, sounds.getPlayerKilled());

            CtfGame ctfGame = cacheManager.getGames().get(victimCtfPlayer.getGameId());
            String killMessage = lang.getPlayerKilledBroadcast()
                .replace("%killer-name%", damager.getName())
                .replace("%victim-name%", victim.getName());
            ctfGame.getPlayers().forEach(ctfPlayer -> {
                Player bukkitPlayer = ctfPlayer.getBukkitPlayer();
                ChatUtils.sendPrefixedMessage(bukkitPlayer, killMessage);
            });

            handleCaptureItemDrop(victim, victimCtfPlayer.getTeam());
            handleDeathState(victim, victimCtfPlayer.getTeam(), ctfGame);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCtfPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (ctfPlayer == null) return;

        CtfGame ctfGame = cacheManager.getGames().get(ctfPlayer.getGameId());
        if (ctfGame == null) return;

        handleCaptureItemDrop(player, ctfPlayer.getTeam());
    }

    private void handleStats(CtfPlayer killer, CtfPlayer victim) {
        victim.setDeaths(victim.getDeaths() + 1);
        killer.setKills(killer.getKills() + 1);
    }

    private void handleCaptureItemDrop(Player player, GameTeam playerTeam) {
        Material captureItem = playerTeam == GameTeam.BLUE ? config.getRedTeamFlag() : config.getBlueTeamFlag();
        if (!player.getInventory().contains(captureItem)) return;

        player.getInventory().remove(captureItem);
        ItemStack captureItemStack = new ItemStack(captureItem, 1);
        player.getWorld().dropItemNaturally(player.getLocation(), captureItemStack);

        Color color = playerTeam == GameTeam.BLUE ? Color.RED : Color.BLUE;
        gameManager.spawnFirework(player.getLocation(), color);
    }

    private void handleDeathState(Player player, GameTeam playerTeam, CtfGame ctfGame) {
        player.setGameMode(GameMode.SPECTATOR);

        Bukkit.getGlobalRegionScheduler().runDelayed(CaptureTheFlag.getInstance(), (task) -> {
            if (!player.isOnline()) return;
            if (ctfGame.getGameStatus() != GameStatus.IN_PROGRESS) return;

            GameLocation spawnLocation = ctfGame.getSpawnLocation(playerTeam);
            player.teleport(spawnLocation.toLocation());
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);

            Title respawnTitle = Title.title(ChatUtils.colorize(lang.getRespawnTitle()),
                ChatUtils.colorize(lang.getRespawnSubtitle()), 5, 40, 5);
            player.showTitle(respawnTitle);
            SoundUtils.sendSoundRaw(player, sounds.getPlayerRespawn());
        }, 100);
    }
}
