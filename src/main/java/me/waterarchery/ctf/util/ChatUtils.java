package me.waterarchery.ctf.util;

import lombok.extern.slf4j.Slf4j;
import me.waterarchery.ctf.CaptureTheFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Slf4j
public class ChatUtils {

    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
        .tags(TagResolver.builder().resolver(StandardTags.defaults()).build())
        .strict(false)
        .emitVirtuals(false)
        .postProcessor(component -> component.decoration(TextDecoration.ITALIC, false))
        .build();

    public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
        .character('§')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();

    public static void sendPrefixedMessage(CommandSender sender, String message) {
        String prefix = CaptureTheFlag.getPluginConfig().getPrefix();
        if (sender instanceof Player player) {
            player.sendMessage(ChatUtils.colorize(prefix + message));
        } else {
            sender.sendMessage(ChatUtils.colorizeLegacy(prefix + message));
        }
    }

    public static Component colorize(String message, TagResolver... placeholders) {
        if (message == null) return Component.text("null text");

        return MINI_MESSAGE.deserialize(message, placeholders);
    }

    public static String colorizeLegacy(String message) {
        if (message == null) return "null text";

        try {
            Component component = MINI_MESSAGE.deserialize(message);
            return LEGACY_COMPONENT_SERIALIZER.serialize(component);
        } catch (Exception e) {
            return LEGACY_COMPONENT_SERIALIZER.serialize(LEGACY_COMPONENT_SERIALIZER.deserialize(message));
        }
    }

}
