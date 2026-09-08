package com.minegolem.zelChatCooldown.manager;

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

    private final ConcurrentMap<UUID, Long> cooldowns =
            new ConcurrentHashMap<>();

    private final NamespacedKey cooldownKey;

    public CooldownManager(NamespacedKey cooldownKey) {
        this.cooldownKey = cooldownKey;
    }

    public CooldownResult tryUse(UUID uuid, Duration duration) {

        long now = System.nanoTime();
        long durationNanos = duration.toNanos();

        AtomicReference<CooldownResult> result =
                new AtomicReference<>();

        cooldowns.compute(uuid, (key, expiresAt) -> {

            if (expiresAt == null || expiresAt <= now) {

                result.set(CooldownResult.success());

                return now + durationNanos;
            }

            result.set(
                    CooldownResult.denied(
                            Duration.ofNanos(expiresAt - now)
                    )
            );

            return expiresAt;
        });

        return result.get();
    }

    public Optional<Duration> getRemaining(UUID uuid) {

        Long expiresAt = cooldowns.get(uuid);

        if (expiresAt == null) {
            return Optional.empty();
        }

        long remaining = expiresAt - System.nanoTime();

        if (remaining <= 0) {
            cooldowns.remove(uuid, expiresAt);
            return Optional.empty();
        }

        return Optional.of(Duration.ofNanos(remaining));
    }

    public void load(Player player) {

        Long expiresAt = player.getPersistentDataContainer().get(
                cooldownKey,
                PersistentDataType.LONG
        );

        if (expiresAt == null) {
            return;
        }

        long remainingMillis =
                expiresAt - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            player.getPersistentDataContainer().remove(cooldownKey);
            return;
        }

        long remainingNanos =
                Duration.ofMillis(remainingMillis).toNanos();

        cooldowns.put(
                player.getUniqueId(),
                System.nanoTime() + remainingNanos
        );
    }

    public void save(Player player) {

        Long expiresAt = cooldowns.get(player.getUniqueId());

        if (expiresAt == null) {
            player.getPersistentDataContainer().remove(cooldownKey);
            return;
        }

        long remainingNanos =
                expiresAt - System.nanoTime();

        if (remainingNanos <= 0) {
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
    }

    public void remove(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    public void cleanup() {
        cooldowns.clear();
    }
}