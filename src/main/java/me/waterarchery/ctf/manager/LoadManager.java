package me.waterarchery.ctf.manager;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.command.CtfCommand;
import me.waterarchery.ctf.configuration.ConfigFile;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.configuration.SoundsFile;
import me.waterarchery.ctf.database.CtfDatabase;
import me.waterarchery.ctf.database.impl.MySQLCtfDatabase;
import me.waterarchery.ctf.database.impl.SQLiteCtfDatabase;
import me.waterarchery.ctf.listener.BugListeners;
import me.waterarchery.ctf.listener.CaptureListeners;
import me.waterarchery.ctf.listener.DeathStateListener;
import me.waterarchery.ctf.listener.PlayerLeaveListener;
import me.waterarchery.ctf.model.game.CtfGame;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;

public class LoadManager {

    private static LoadManager instance;

    private final Logger logger;

    public static LoadManager getInstance() {
        if (instance == null) {
            instance = new LoadManager();
        }

        return instance;
    }

    private LoadManager() {
        CaptureTheFlag plugin = CaptureTheFlag.getInstance();
        logger = plugin.getSLF4JLogger();
    }

    public void load() {
        loadConfigs();
        loadCommands();
        loadListeners();
        loadDatabase();
    }

    public void unload() {
        CommandManager commandManager = CommandManager.getInstance();
        commandManager.unregisterCommands();

        CtfDatabase database = CaptureTheFlag.getDatabase();
        database.close();
    }

    public void reload() {
        CaptureTheFlag.getPluginConfig().load(true);
        CaptureTheFlag.getLang().load(true);
        CaptureTheFlag.getSounds().load(true);
    }

    private void loadConfigs() {
        ConfigFile configFile = loadOkaeriConfig(ConfigFile.class, "config");
        CaptureTheFlag.setPluginConfig(configFile);

        LangFile langFile = loadOkaeriConfig(LangFile.class, "lang");
        CaptureTheFlag.setLang(langFile);

        SoundsFile soundsFile = loadOkaeriConfig(SoundsFile.class, "sounds");
        CaptureTheFlag.setSounds(soundsFile);

        logger.info("Loaded configs");
    }

    private void loadCommands() {
        CommandManager commandManager = CommandManager.getInstance();
        commandManager.registerCommand(new CtfCommand());

        logger.info("Loaded commands");
    }

    private void loadListeners() {
        CaptureTheFlag plugin = CaptureTheFlag.getInstance();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new DeathStateListener(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new CaptureListeners(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new BugListeners(), plugin);

        logger.info("Loaded listeners");
    }

    private void loadDatabase() {
        ConfigFile.DatabaseConfiguration databaseConfiguration = CaptureTheFlag.getPluginConfig().getDatabase();
        CtfDatabase database = databaseConfiguration.getType().equalsIgnoreCase("mysql") ?
            new MySQLCtfDatabase() :
            new SQLiteCtfDatabase();
        CaptureTheFlag.setDatabase(database);

        List<CtfGame> ctfGames = database.loadGames();
        CacheManager cacheManager = CacheManager.getInstance();
        ctfGames.forEach(game -> cacheManager.getGames().put(game.getId(), game));
        logger.info("Loaded {} games from database", ctfGames.size());
    }

    private <T extends OkaeriConfig> T loadOkaeriConfig(Class<T> clazz, String fileName) {
        if (!fileName.endsWith(".yml")) fileName += ".yml";

        CaptureTheFlag plugin = CaptureTheFlag.getInstance();
        File configFolder = new File(plugin.getDataFolder(), fileName);
        T config = ConfigManager.create(clazz, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(configFolder);
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        plugin.getSLF4JLogger().info("Loaded config file named: {}", fileName);
        return config;
    }
}
