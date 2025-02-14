package org.example;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import opg.p2vman.moduleloader.module.JavaModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ExempleModule extends JavaModule implements Listener {
    @Override
    public void onEnable() {
        getLogger().info("Hi from module");
        ExempleClass.test();
        getServer().getPluginManager().registerEvents(this, getPlugin());
    }

    @EventHandler
    public static void chatevent(AsyncChatEvent event) {
        event.message(Component.text("AAAAAAAAAAAAAa"));
    }
}
