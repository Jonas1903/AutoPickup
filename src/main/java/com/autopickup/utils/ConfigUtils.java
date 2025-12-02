package com.autopickup.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
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

    /**
     * Formats a Material name to a human-readable string.
     * e.g., IRON_INGOT -> "Iron Ingot"
     */
    public static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
