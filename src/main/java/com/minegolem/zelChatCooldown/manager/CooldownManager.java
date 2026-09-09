package com.minegolem.zelChatCooldown.manager;

import com.minegolem.zelChatCooldown.ZelChatCooldown;
import com.minegolem.zelChatCooldown.model.CooldownResult;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public final class CooldownManager {

    private final ZelChatCooldown plugin;

    private final ConcurrentMap<UUID, Long> cooldowns =
            new ConcurrentHashMap<>();

    private final NamespacedKey cooldownKey;

    public CooldownManager(ZelChatCooldown plugin, NamespacedKey cooldownKey) {
        this.plugin = plugin;
        this.cooldownKey = cooldownKey;

        plugin.debug("CooldownManager initialized.");
    }

    public CooldownResult tryUse(UUID uuid, Duration duration) {

        plugin.debug("Trying to use cooldown for " + uuid
                + " with duration " + duration.toMillis() + "ms.");

        long now = System.nanoTime();
        long durationNanos = duration.toNanos();

        AtomicReference<CooldownResult> result =
                new AtomicReference<>();

        cooldowns.compute(uuid, (key, expiresAt) -> {

            if (expiresAt == null) {

                plugin.debug("No existing cooldown found for " + uuid
                        + ". Cooldown will be created.");

                result.set(CooldownResult.success());

                return now + durationNanos;
            }

            if (expiresAt <= now) {

                plugin.debug("Existing cooldown for " + uuid
                        + " has expired. Creating a new cooldown.");

                result.set(CooldownResult.success());

                return now + durationNanos;
            }

            Duration remaining =
                    Duration.ofNanos(expiresAt - now);

            plugin.debug("Cooldown denied for " + uuid
                    + ". Remaining: " + remaining.toMillis() + "ms.");

            result.set(
                    CooldownResult.denied(remaining)
            );

            return expiresAt;
        });

        return result.get();
    }

    public Optional<Duration> getRemaining(UUID uuid) {

        Long expiresAt = cooldowns.get(uuid);

        if (expiresAt == null) {
            plugin.debug("No cooldown found for " + uuid + ".");
            return Optional.empty();
        }

        long remaining = expiresAt - System.nanoTime();

        if (remaining <= 0) {

            plugin.debug("Cooldown for " + uuid
                    + " has expired. Removing it.");

            cooldowns.remove(uuid, expiresAt);

            return Optional.empty();
        }

        Duration duration = Duration.ofNanos(remaining);

        plugin.debug("Remaining cooldown for " + uuid
                + ": " + duration.toMillis() + "ms.");

        return Optional.of(duration);
    }

    public void load(Player player) {

        plugin.debug("Loading cooldown for player "
                + player.getName() + " (" + player.getUniqueId() + ").");

        Long expiresAt = player.getPersistentDataContainer().get(
                cooldownKey,
                PersistentDataType.LONG
        );

        if (expiresAt == null) {

            plugin.debug("No persisted cooldown found for "
                    + player.getName() + ".");

            return;
        }

        long remainingMillis =
                expiresAt - System.currentTimeMillis();

        if (remainingMillis <= 0) {

            plugin.debug("Persisted cooldown for "
                    + player.getName() + " has expired. Removing it.");

            player.getPersistentDataContainer().remove(cooldownKey);

            return;
        }

        long remainingNanos =
                Duration.ofMillis(remainingMillis).toNanos();

        cooldowns.put(
                player.getUniqueId(),
                System.nanoTime() + remainingNanos
        );

        plugin.debug("Loaded cooldown for "
                + player.getName()
                + ". Remaining: "
                + remainingMillis
                + "ms.");
    }

    public void save(Player player) {

        plugin.debug("Saving cooldown for player "
                + player.getName() + " (" + player.getUniqueId() + ").");

        Long expiresAt = cooldowns.get(player.getUniqueId());

        if (expiresAt == null) {

            plugin.debug("No active cooldown found for "
                    + player.getName() + ". Removing persisted value.");

            player.getPersistentDataContainer().remove(cooldownKey);

            return;
        }

        long remainingNanos =
                expiresAt - System.nanoTime();

        if (remainingNanos <= 0) {

            plugin.debug("Cooldown for "
                    + player.getName()
                    + " has expired before saving. Removing it.");

            player.getPersistentDataContainer().remove(cooldownKey);
            cooldowns.remove(player.getUniqueId());

            return;
        }

        long expiresAtMillis =
                System.currentTimeMillis()
                        + Duration.ofNanos(remainingNanos).toMillis();

        player.getPersistentDataContainer().set(
                cooldownKey,
                PersistentDataType.LONG,
                expiresAtMillis
        );

        plugin.debug("Saved cooldown for "
                + player.getName()
                + ". Remaining: "
                + Duration.ofNanos(remainingNanos).toMillis()
                + "ms.");
    }

    public void remove(Player player) {

        plugin.debug("Removing cooldown for "
                + player.getName() + ".");

        cooldowns.remove(player.getUniqueId());
    }

    public void cleanup() {

        plugin.debug("Cleaning up all cooldowns. "
                + "Current size: " + cooldowns.size());

        cooldowns.clear();
    }
}