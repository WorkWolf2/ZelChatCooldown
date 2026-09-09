package com.minegolem.zelChatCooldown.command;


import com.minegolem.zelChatCooldown.ZelChatCooldown;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class ReloadCommand implements CommandExecutor {

    private final ZelChatCooldown plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {

        if (!sender.hasPermission("zelchatcooldown.reload")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<red>Non hai il permesso per eseguire questo comando."
            ));
            return true;
        }

        try {
            plugin.reload();
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<green>Configurazione ricaricata con successo!"
            ));
        } catch (Exception exception) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<red>Errore durante il reload della configurazione: <yellow>" + exception.getMessage()
            ));
            plugin.getLogger().severe("Errore durante il reload: " + exception.getMessage());
            exception.printStackTrace();
        }

        return true;
    }
}