package me.waterarchery.ctf.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.List;

@Getter
@Setter
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ConfigFile extends OkaeriConfig {

    private String prefix = "<aqua>[ᴄᴛғ] ";
    private int gameEndTimeoutInMinutes = 10;
    private int gameStartWarmupTime = 10;
    private int baseCaptureRadius = 3;
    private int capturePerGame = 3;
    private int scoreBoardUpdateInTicks = 20;

    private Material redTeamFlag = Material.RED_WOOL;
    private Material blueTeamFlag = Material.CYAN_WOOL;

    private int redTeamArmorColor = 0xFF0000;
    private int blueTeamArmorColor = 0x0000FF;

    private List<Integer> gameWarmupTimeBroadcast = List.of(10, 5, 3, 2, 1);

    private DatabaseConfiguration database = new DatabaseConfiguration();

    @Getter
    @Setter
    public static class DatabaseConfiguration extends OkaeriConfig {

        @Comment("sqlite, mysql")
        private String type = "sqlite";

        private String host = "localhost";
        private int port = 3306;
        private String database = "ctf";
        private String username = "root";
        private String password = "";

        private int minimumIdle = 2;
        private int maximumPoolSize = 10;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
    }
}
