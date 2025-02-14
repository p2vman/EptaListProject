package opg.p2vman.moduleloader.api;

import org.bukkit.Server;

import java.util.logging.Logger;

public interface Module {
    void onLoad();
    void onEnable();
    void onDisable();
    String getName();
    ResourceMannager getResourceMannager();
    Server getServer();
    Logger getLogger();
}
