package com.minegolem.zelChatCooldown.placeholder;

import com.minegolem.zelChatCooldown.manager.CooldownManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

public final class CooldownPlaceholder extends PlaceholderExpansion {

    private final CooldownManager cooldownManager;

    public CooldownPlaceholder(CooldownManager cooldownManager) {
        this.cooldownManager = cooldownManager;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "zelchatcooldown";
    }

    @Override
    public @NonNull String getAuthor() {
        return "MineGolem";
    }

    @Override
    public @NonNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, String params) {

        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("remaining")) {

            Optional<Duration> remaining =
                    cooldownManager.getRemaining(
                            player.getUniqueId()
                    );

            return remaining.map(duration -> String.format(
                    "%.2f",
                    duration.toNanos() / 1_000_000_000.0
            )).orElse("0.00");

        }

        return null;
    }
}