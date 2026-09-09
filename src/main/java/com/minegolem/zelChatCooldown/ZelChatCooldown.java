package com.minegolem.zelChatCooldown;

import com.google.common.base.Preconditions;
import com.minegolem.zelChatCooldown.chat.CooldownChatModule;
import com.minegolem.zelChatCooldown.command.ReloadCommand;
import com.minegolem.zelChatCooldown.config.CooldownConfig;
import com.minegolem.zelChatCooldown.listener.PlayerListener;
import com.minegolem.zelChatCooldown.manager.CooldownManager;
import com.minegolem.zelChatCooldown.placeholder.CooldownPlaceholder;
import it.pino.zelchat.api.ZelChatAPI;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ZelChatCooldown extends JavaPlugin {

    @Getter
    private CooldownConfig cooldownConfig;
    @Getter
    private CooldownManager cooldownManager;
    private final NamespacedKey cooldownKey =
            new NamespacedKey(this, "cooldown");

    public static ZelChatCooldown INSTANCE;

    @Override
    public void onEnable() {

        INSTANCE = this;

        saveDefaultConfig();

        this.cooldownConfig =
                new CooldownConfig(getConfig());

        this.cooldownManager =
                new CooldownManager(this, cooldownKey);

        getServer().getPluginManager().registerEvents(
                new PlayerListener(cooldownManager),
                this
        );

        var module = new CooldownChatModule(this);
        getZelchatAPI().getModuleManager().register(this, module);

        new CooldownPlaceholder(cooldownManager).register();

        Objects.requireNonNull(getCommand("zelchatcooldownreload")).setExecutor(new ReloadCommand(this));
    }

    @Override
    public void onDisable() {
        // Nothing
    }

    public @NotNull ZelChatAPI getZelchatAPI() {
        return Preconditions.checkNotNull(ZelChatAPI.get(), "ZelChatAPI has not been initialized yet!");
    }

    public void debug(String message) {
        if (!this.getCooldownConfig().isDebug()) {
            return;
        }

        this.getLogger().info("[DEBUG] " + message);
    }

    public void reload() {
        reloadConfig();

        this.cooldownConfig = new CooldownConfig(getConfig());

        debug("Configuration reloaded.");
    }

    public Component parseItemName(final @NotNull ItemStack item) {
        if (item.getItemMeta() == null) return Component.text("");

        if (!item.getItemMeta().hasItemName() && !item.getItemMeta().hasCustomName()) return parseVanillaName(item);

        if (item.getItemMeta().hasCustomName()) {

            final var amount = (item.getAmount() <= 1) ? "" : item.getAmount() + "x ";
            final var displayName = item.getItemMeta().customName();

            return Component.text(amount).append(displayName);
        }

        final var amount = (item.getAmount() <= 1) ? "" : item.getAmount() + "x ";
        final var itemName = item.getItemMeta().itemName();

        return Component.text(amount).append(itemName);
    }

    private Component parseVanillaName(final @NotNull ItemStack item) {
        final var amount = (item.getAmount() <= 1) ? "" : item.getAmount() + "x ";

        final var name = Arrays.stream(
                item.getType()
                        .name()
                        .toLowerCase()
                        .split("_")
                )
                .map(s -> s.substring(0, 1).toUpperCase()
                                + s.substring(1)
                )
                .collect(Collectors.joining(" "));

        return Component.text(amount + name);
    }
}
