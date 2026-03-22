package me.waterarchery.ctf.manager;

import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.util.ChatUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class BossBarManager {

    private static BossBarManager instance;

    private final CacheManager cacheManager;
    private final LangFile lang;

    public static BossBarManager getInstance() {
        if (instance == null) {
            instance = new BossBarManager();
        }
        return instance;
    }

    private BossBarManager() {
        cacheManager = CacheManager.getInstance();
        lang = CaptureTheFlag.getLang();
    }

    public void addBossBar(Player player, CtfGame ctfGame) {
        BossBar bossBar = cacheManager.getBossbars().get(ctfGame.getId());
        if (bossBar == null) bossBar = createBossBar(ctfGame);

        bossBar.addViewer(player);
    }

    public void removeBossBar(Player player, CtfGame ctfGame) {
        BossBar bossBar = cacheManager.getBossbars().get(ctfGame.getId());
        if (bossBar != null) {
            bossBar.removeViewer(player);
        }
    }

    public void updateBossBar(CtfGame ctfGame) {
        int redScore = ctfGame.getRedTeam().getCaptures();
        int blueScore = ctfGame.getBlueTeam().getCaptures();
        String bossBarText = lang.getBossBarText()
            .replace("%red-score%", String.valueOf(redScore))
            .replace("%blue-score%", String.valueOf(blueScore));

        BossBar bossBar = cacheManager.getBossbars().get(ctfGame.getId());
        bossBar.name(ChatUtils.colorize(bossBarText));
    }

    private BossBar createBossBar(CtfGame ctfGame) {
        BossBar bossBar = BossBar.bossBar(Component.text(), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        cacheManager.getBossbars().put(ctfGame.getId(), bossBar);

        updateBossBar(ctfGame);
        return bossBar;
    }
}
