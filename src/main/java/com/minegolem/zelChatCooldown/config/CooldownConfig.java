package com.minegolem.zelChatCooldown.config;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.Duration;
import java.util.*;

public final class CooldownConfig {

    private final Map<String, Duration> cooldowns;
    @Getter
    private final String cooldownMessage;
    private final boolean actionbar;

    @Getter
    private final Duration defaultCooldown;

    @Getter
    private final List<String> prefixes;

    @Getter
    private final boolean debug;

    @Getter
    private final String bypassPermission;

    @Getter
    private final boolean soundConfirm;
    @Getter
    private final Sound sound;

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

        this.debug = config.getBoolean("debug", false);

        this.defaultCooldown = config.contains("settings.default-cooldown")
                ? Duration.ofSeconds(config.getLong("settings.default-cooldown"))
                : null;

        this.bypassPermission = config.getString(
                "settings.bypass-permission",
                "zelchatcooldown.bypass"
        );

        File configFile = new File(Bukkit.getPluginsFolder(), "ZelChat/settings.yml");

        YamlConfiguration zelChatSettings = YamlConfiguration.loadConfiguration(configFile);

        List<String> prefixes = new ArrayList<>();
        prefixes.add(zelChatSettings.getString("Features.inventory-show.parser"));
        prefixes.add(zelChatSettings.getString("Features.item-show.parser"));
        prefixes.add(zelChatSettings.getString("Features.enderchest-show.parser"));

        this.prefixes = prefixes;

        this.soundConfirm = config.getBoolean("settings.enable-sound", true);

        String entityPlayerLevelup = config.getString("settings.sound", "ENTITY_PLAYER_LEVELUP");
        this.sound = Registry.SOUNDS.get(Key.key(entityPlayerLevelup.toLowerCase(Locale.ROOT)));
    }

    public boolean hasBypass(Player player) {
        return player.hasPermission(bypassPermission);
    }

    public Optional<Duration> getCooldown(Player player) {
        Optional<Duration> matched = cooldowns.entrySet()
                .stream()
                .filter(entry -> player.hasPermission(entry.getKey()))
                .map(Map.Entry::getValue)
                .min(Comparator.naturalOrder());

        if (matched.isPresent()) {
            return matched;
        }

        return Optional.ofNullable(defaultCooldown);
    }

    public boolean isActionbarEnabled() {
        return actionbar;
    }

    public boolean isSoundEnabled() {
        return soundConfirm;
    }
}