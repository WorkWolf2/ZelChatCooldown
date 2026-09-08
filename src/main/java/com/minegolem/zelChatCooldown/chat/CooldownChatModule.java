package com.minegolem.zelChatCooldown.chat;

import com.minegolem.zelChatCooldown.ZelChatCooldown;
import com.minegolem.zelChatCooldown.model.CooldownResult;
import it.pino.zelchat.api.message.ChatMessage;
import it.pino.zelchat.api.message.state.MessageState;
import it.pino.zelchat.api.module.ChatModule;
import it.pino.zelchat.api.module.annotation.ChatModuleSettings;
import it.pino.zelchat.api.module.priority.ModulePriority;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.print.Paper;
import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@ChatModuleSettings(pluginOwner = "ZKraft-Addon", priority = ModulePriority.HIGHEST)
public class CooldownChatModule implements ChatModule {

    private final ZelChatCooldown plugin;

    @Override
    public void handleChatMessage(@NotNull ChatMessage chatMessage) {

        Player player = chatMessage.getBukkitPlayer();

        if (!plugin.getPrefix().containsPrefix(chatMessage.getMessage())) return;

        Optional<Duration> duration =
                plugin.getCooldownConfig().getCooldown(player);

        if (duration.isEmpty()) {
            return;
        }

        CooldownResult result =
                plugin.getCooldownManager().tryUse(
                        player.getUniqueId(),
                        duration.get()
                );

        if (result.allowed()) return;

        chatMessage.setState(MessageState.CANCELLED);

        assert plugin.getCooldownConfig().getCooldownMessage() != null;
        String message = PlaceholderAPI.setPlaceholders(
                player,
                plugin.getCooldownConfig().getCooldownMessage()
        );

        Component component = MiniMessage.miniMessage()
                .deserialize(message);

        if (plugin.getCooldownConfig().isActionbarEnabled()) {
            player.sendActionBar(component);
        }
    }
}

