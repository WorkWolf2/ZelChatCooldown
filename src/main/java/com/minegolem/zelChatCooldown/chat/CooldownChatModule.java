package com.minegolem.zelChatCooldown.chat;

import com.minegolem.zelChatCooldown.ZelChatCooldown;
import com.minegolem.zelChatCooldown.model.CooldownResult;
import com.minegolem.zelChatCooldown.model.Prefix;
import it.pino.zelchat.api.message.ChatMessage;
import it.pino.zelchat.api.message.channel.ChannelType;
import it.pino.zelchat.api.message.state.MessageState;
import it.pino.zelchat.api.module.ChatModule;
import it.pino.zelchat.api.module.annotation.ChatModuleSettings;
import it.pino.zelchat.api.module.priority.ModulePriority;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@ChatModuleSettings(pluginOwner = "ZKraft-Addon", priority = ModulePriority.HIGHEST)
public class CooldownChatModule implements ChatModule {

    private final ZelChatCooldown plugin;

    @Override
    public void handleChatMessage(@NotNull ChatMessage chatMessage) {

        if (chatMessage.getState() == MessageState.CANCELLED || chatMessage.getChannel().getType() == ChannelType.STAFF) {
            return;
        }

        Player player = chatMessage.getBukkitPlayer();

        plugin.debug("Chat message received from " + player.getName() + ": \"" + chatMessage.getMessage() + "\"");

        List<String> prefixes = plugin.getCooldownConfig()
                .getPrefixes()
                .stream()
                .map(prefix -> resolvePrefix(player, prefix))
                .map(prefix -> MiniMessage.miniMessage().deserialize(prefix))
                .map(component -> PlainTextComponentSerializer.plainText().serialize(component))
                .toList();

        plugin.debug(plugin.getCooldownConfig().getPrefixes().toString());

        Prefix prefix = new Prefix(prefixes);

        if (!prefix.containsPrefix(chatMessage.getMessage())) {
            plugin.debug("Message does not contain a configured prefix. Ignoring.");
            return;
        }

        plugin.debug("Message contains a configured prefix.");

        if (plugin.getCooldownConfig().hasBypass(player)) {
            plugin.debug("Player " + player.getName() + " has bypass permission. Skipping cooldown.");
            return;
        }

        Optional<Duration> durationOptional =
                plugin.getCooldownConfig().getCooldown(player);

        if (durationOptional.isEmpty()) {
            plugin.debug("Player " + player.getName() + " is exempt from cooldown.");
            return;
        }

        Duration duration = durationOptional.get();

        plugin.debug("Cooldown configured for " + player.getName() + ": " + duration.toMillis() + "ms");

        CooldownResult result = plugin.getCooldownManager()
                .tryUse(player.getUniqueId(), duration);

        plugin.debug("Cooldown check result for " + player.getName() + ": allowed=" + result.allowed());

        if (result.allowed()) {
            plugin.debug("Message allowed for " + player.getName() + ".");
            return;
        }

        plugin.debug("Message cancelled for " + player.getName() + " because the cooldown is still active.");

        chatMessage.setState(MessageState.CANCELLED);

        String rawMessage = plugin.getCooldownConfig().getCooldownMessage();

        if (rawMessage == null || rawMessage.isBlank()) {
            plugin.debug("Cooldown message is null or empty. " + "Skipping message display.");
            return;
        }

        String message = PlaceholderAPI.setPlaceholders(player, rawMessage);

        plugin.debug(
                "Cooldown message after PlaceholderAPI: " + message
        );

        Component component = MiniMessage.miniMessage().deserialize(message);

        if (plugin.getCooldownConfig().isActionbarEnabled()) {

            plugin.debug("Sending cooldown message to actionbar of " + player.getName() + ".");
            player.sendActionBar(component);
        } else {

            plugin.debug("Sending cooldown message to chat of " + player.getName() + ".");
            player.sendMessage(component);
        }

        if (plugin.getCooldownConfig().isSoundEnabled()) {
            assert plugin.getCooldownConfig().getSound() != null;
            player.playSound(player.getLocation(), plugin.getCooldownConfig().getSound(), 1, 1);
        }
    }

    private String resolvePrefix(Player player, String prefix) {

        if (prefix == null) return "";

        // PlaceholderAPI
        prefix = PlaceholderAPI.setPlaceholders(player, prefix);

        // %item%
        if (prefix.contains("%item%")) {

            ItemStack item = player.getInventory().getItemInMainHand();

            Component itemName = plugin.parseItemName(item);

            String plainItemName = PlainTextComponentSerializer.plainText().serialize(itemName);

            prefix = prefix.replace("%item%", plainItemName);

            plugin.debug("Resolved %item% for " + player.getName() + " to \"" + plainItemName + "\".");
        }

        return prefix;
    }
}