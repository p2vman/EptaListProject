package org.eptalist.bounge;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Event implements Listener {
    @EventHandler(
            priority = 5
    )
    public void onLogin(PreLoginEvent event) {
        PendingConnection c = event.getConnection();

        if (Boungecord.config.get().enable) {
            if (!(Boungecord.list.is(c.getName()))) {
                c.disconnect(new TextComponent(Boungecord.mode.kick_msg));
                event.setCancelled(true);
            }
        }
    }
}
