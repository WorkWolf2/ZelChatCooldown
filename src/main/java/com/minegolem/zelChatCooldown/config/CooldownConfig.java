package com.minegolem.zelChatCooldown.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;

public final class CooldownConfig {

    private final Map<String, Duration> cooldowns;
    @Getter
    private final String cooldownMessage;
    private final boolean actionbar;

    @Getter
    private final List<String> prefixes;

    public CooldownConfig(FileConfiguration config) {
        Map<String, Duration> loaded = new HashMap<>();

        ConfigurationSection section =
                config.getConfigurationSection("cooldowns");

        if (section != null) {
            for (String permission : section.getKeys(false)) {

                long seconds = section.getLong(permission);

                if (seconds < 0) {
                    continue;
                }

                loaded.put(
                        permission,
                        Duration.ofSeconds(seconds)
                );
            }
        }

        this.cooldowns = Map.copyOf(loaded);

        this.cooldownMessage = config.getString(
                "messages.cooldown",
                "<red>Devi aspettare ancora <yellow><remaining> <red>secondi!"
        );

        this.actionbar = config.getBoolean(
                "settings.actionbar",
                true
        );

        this.prefixes = config.getStringList("prefixes");
    }

    public Optional<Duration> getCooldown(Player player) {
        return cooldowns.entrySet()
                .stream()
                .filter(entry -> player.hasPermission(entry.getKey()))
                .map(Map.Entry::getValue)
                .min(Comparator.naturalOrder());
    }

    public boolean isActionbarEnabled() {
        return actionbar;
    }
}