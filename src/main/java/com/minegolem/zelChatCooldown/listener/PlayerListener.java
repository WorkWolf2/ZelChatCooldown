package com.minegolem.zelChatCooldown.listener;

import com.minegolem.zelChatCooldown.manager.CooldownManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public final class PlayerListener implements Listener {

    private final CooldownManager cooldownManager;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cooldownManager.load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldownManager.save(event.getPlayer());
        cooldownManager.remove(event.getPlayer());
    }
}
