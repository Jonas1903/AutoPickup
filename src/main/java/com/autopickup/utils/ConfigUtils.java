package com.autopickup.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUtils {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static Component colorize(String message) {
        return SERIALIZER.deserialize(message);
    }

    public static String getMessage(FileConfiguration config, String path) {
        String prefix = config.getString("messages.prefix", "&8[&6AutoPickup&8] ");
        String message = config.getString("messages." + path, "");
        return prefix + message;
    }

    public static Component getColoredMessage(FileConfiguration config, String path) {
        return colorize(getMessage(config, path));
    }

    public static Component getColoredMessageWithPlaceholder(FileConfiguration config, String path, String placeholder, String value) {
        String message = getMessage(config, path);
        message = message.replace(placeholder, value);
        return colorize(message);
    }
}
