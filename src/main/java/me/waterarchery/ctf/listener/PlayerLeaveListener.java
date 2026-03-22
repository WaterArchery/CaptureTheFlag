package me.waterarchery.ctf.listener;

import me.waterarchery.ctf.manager.CacheManager;
import me.waterarchery.ctf.manager.GameManager;
import me.waterarchery.ctf.model.player.CtfPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    private final CacheManager cacheManager = CacheManager.getInstance();
    private final GameManager gameManager = GameManager.getInstance();

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (ctfPlayer != null) {
            gameManager.leaveGame(player);
        }
    }
}
