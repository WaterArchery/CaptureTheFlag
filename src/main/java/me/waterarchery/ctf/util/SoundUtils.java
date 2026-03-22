package me.waterarchery.ctf.util;

import me.waterarchery.ctf.CaptureTheFlag;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class SoundUtils {

    private static FileConfiguration sounds;

    public static void sendSound(Player p, String configPath) {
        if (sounds == null) {
            File dataFolder = CaptureTheFlag.getInstance().getDataFolder();
            File soundsFile = new File(dataFolder, "sounds.yml");
            sounds = YamlConfiguration.loadConfiguration(soundsFile);
        }

        String soundName = sounds.getString(configPath);
        if (soundName == null) return;
        if (soundName.isBlank()) return;

        sendSoundRaw(p, soundName);
    }

    public static void sendSoundRaw(Player p, String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName);
            sendSound(p, sound);
        } catch (Exception e) {
            CaptureTheFlag.getInstance().getSLF4JLogger().error("Error sound playing: {}", soundName);
        }
    }

    public static void sendSound(Player p, Sound sound) {
        p.playSound(p, sound, 1, 1);
    }
}
