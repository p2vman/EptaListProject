package io.github.p2vman.eptalist.spigot;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import io.github.p2vman.eptalist.Config;

public class Event implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onConnect(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        if (p != null) {
            Config config = EptaList.config.get();
            if (config.enable) {
                if (!((config.skip_to_op && p.isOp()) || EptaList.list.is(p.getName()))) {
                    e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatColor.translateAlternateColorCodes('&', EptaList.mode.kick_msg));
                }
            }
        }
    }
}
