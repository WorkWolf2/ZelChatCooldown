package com.minegolem.zelChatCooldown;

import com.google.common.base.Preconditions;
import com.minegolem.zelChatCooldown.chat.CooldownChatModule;
import com.minegolem.zelChatCooldown.config.CooldownConfig;
import com.minegolem.zelChatCooldown.listener.PlayerListener;
import com.minegolem.zelChatCooldown.manager.CooldownManager;
import com.minegolem.zelChatCooldown.model.Prefix;
import com.minegolem.zelChatCooldown.placeholder.CooldownPlaceholder;
import it.pino.zelchat.api.ZelChatAPI;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ZelChatCooldown extends JavaPlugin {

    @Getter
    private CooldownConfig cooldownConfig;
    @Getter
    private CooldownManager cooldownManager;
    @Getter
    private Prefix prefix;
    private final NamespacedKey cooldownKey =
            new NamespacedKey(this, "cooldown");

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.cooldownConfig =
                new CooldownConfig(getConfig());

        this.cooldownManager =
                new CooldownManager(cooldownKey);

        this.prefix =
                new Prefix(this.cooldownConfig.getPrefixes());

        getServer().getPluginManager().registerEvents(
                new PlayerListener(cooldownManager),
                this
        );

        var module = new CooldownChatModule(this);
        getZelchatAPI().getModuleManager().register(this, module);

        new CooldownPlaceholder(cooldownManager).register();
    }

    @Override
    public void onDisable() {
        // Nothing
    }

    public @NotNull ZelChatAPI getZelchatAPI() {
        return Preconditions.checkNotNull(ZelChatAPI.get(), "ZelChatAPI has not been initialized yet!");
    }
}
