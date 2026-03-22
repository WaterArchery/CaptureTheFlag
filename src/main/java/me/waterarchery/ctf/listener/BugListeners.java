package me.waterarchery.ctf.listener;

import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.configuration.SoundsFile;
import me.waterarchery.ctf.manager.CacheManager;
import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.model.game.constant.GameStatus;
import me.waterarchery.ctf.model.player.CtfPlayer;
import me.waterarchery.ctf.util.ChatUtils;
import me.waterarchery.ctf.util.SoundUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class BugListeners implements Listener {

    private final CacheManager cacheManager = CacheManager.getInstance();
    private final LangFile lang;
    private final SoundsFile sounds;

    public BugListeners() {
        lang = CaptureTheFlag.getLang();
        sounds = CaptureTheFlag.getSounds();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (ctfPlayer == null) return;

        ChatUtils.sendPrefixedMessage(player, lang.getCantBlockBreak());
        SoundUtils.sendSoundRaw(player, sounds.getCantBlockBreak());
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (ctfPlayer == null) return;

        ChatUtils.sendPrefixedMessage(player, lang.getCantBlockPlace());
        SoundUtils.sendSoundRaw(player, sounds.getCantBlockPlace());
        event.setCancelled(true);
    }

    @EventHandler
    public void onPreGameDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim) {
            CtfPlayer victimCtfPlayer = cacheManager.getPlayers().get(victim.getUniqueId());
            if (victimCtfPlayer == null) return;

            CtfGame ctfGame = cacheManager.getGames().get(victimCtfPlayer.getGameId());
            if (ctfGame == null || ctfGame.getGameStatus() == GameStatus.IN_PROGRESS) return;

            if (event.getDamager() instanceof Player attacker) {
                ChatUtils.sendPrefixedMessage(attacker, lang.getCantDamageUntilGameStart());
                SoundUtils.sendSoundRaw(attacker, sounds.getCantDamageUntilGameStart());
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        CtfPlayer victimCtfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (victimCtfPlayer == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodDeplate(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            CtfPlayer victimCtfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
            if (victimCtfPlayer == null) return;

            event.setCancelled(true);
        }
    }
}
