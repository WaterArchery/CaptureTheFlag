package me.waterarchery.ctf.manager;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionResolver;
import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.ConfigFile;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.model.command.BaseCommand;
import me.waterarchery.ctf.model.player.constant.GameTeam;
import me.waterarchery.ctf.util.ChatUtils;
import me.waterarchery.ctf.util.SoundUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {

    private static CommandManager instance;

    private final List<BaseCommand> commands = new ArrayList<>();
    private final Logger logger;
    private final BukkitCommandManager<CommandSender> manager;

    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }

        return instance;
    }

    private CommandManager() {
        CaptureTheFlag plugin = CaptureTheFlag.getInstance();

        logger = plugin.getSLF4JLogger();
        manager = BukkitCommandManager.create(plugin);

        registerSuggestions();
        registerMessages();
    }

    public void registerCommand(BaseCommand command) {
        logger.info("Registering command: {}", command.getClass().getSimpleName());
        manager.registerCommand(command);
        commands.add(command);
    }

    public void unregisterCommands() {
        commands.forEach(c -> {
            logger.info("Unregistering command: {}", c.getClass().getSimpleName());
            manager.unregisterCommand(c);
        });
    }

    public void registerSuggestion(SuggestionKey key, @NotNull SuggestionResolver.Simple<CommandSender> suggestionResolver) {
        manager.registerSuggestion(key, suggestionResolver);
    }

    private void registerSuggestions() {
        registerSuggestion(SuggestionKey.of("ctf-games"), (sender) -> {
            CacheManager cacheManager = CacheManager.getInstance();
            return cacheManager.getGames().keySet().stream().toList();
        });

        registerSuggestion(SuggestionKey.of("ctf-teams"), (sender) -> Arrays.stream(GameTeam.values()).map(GameTeam::name).toList());
    }

    private void registerMessages() {
        ConfigFile configFile = CaptureTheFlag.getPluginConfig();
        LangFile lang = CaptureTheFlag.getLang();
        String prefix = configFile.getPrefix();

        manager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> {
            sender.sendMessage(ChatUtils.colorize(prefix + lang.getTooFewArgs()));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });

        manager.registerMessage(MessageKey.TOO_MANY_ARGUMENTS, (sender, context) -> {
            sender.sendMessage(ChatUtils.colorize(prefix + lang.getTooManyArgs()));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });

        manager.registerMessage(MessageKey.INVALID_ARGUMENT, (sender, context) -> {
            sender.sendMessage(ChatUtils.colorize(prefix + lang.getInvalidArg()));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });

        manager.registerMessage(MessageKey.UNKNOWN_COMMAND, (sender, context) -> {
            sender.sendMessage(ChatUtils.colorize(prefix + lang.getUnknownCommand()));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });

        manager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> {
            sender.sendMessage(ChatUtils.colorize(prefix + lang.getNoPermission()));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });
    }
}
