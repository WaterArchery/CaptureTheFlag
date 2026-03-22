package me.waterarchery.ctf;

import lombok.Getter;
import lombok.Setter;
import me.waterarchery.ctf.configuration.ConfigFile;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.configuration.SoundsFile;
import me.waterarchery.ctf.database.CtfDatabase;
import me.waterarchery.ctf.manager.LoadManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CaptureTheFlag extends JavaPlugin {

    @Getter
    @Setter
    private static CtfDatabase database;

    @Getter
    @Setter
    private static ConfigFile pluginConfig;

    @Getter
    @Setter
    private static LangFile lang;

    @Getter
    @Setter
    private static SoundsFile sounds;

    @Override
    public void onEnable() {
        LoadManager.getInstance().load();
    }

    @Override
    public void onDisable() {
        LoadManager.getInstance().unload();
    }

    public static CaptureTheFlag getInstance() {
        return JavaPlugin.getPlugin(CaptureTheFlag.class);
    }
}
