package org.eptalist.bounge;

import io.github.p2vman.Identifier;
import io.github.p2vman.profiling.ExempleProfiler;
import io.github.p2vman.profiling.Profiler;
import net.md_5.bungee.api.plugin.Plugin;
import org.eptalist.Config;
import org.eptalist.storge.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class Boungecord extends Plugin {
    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("EptaList");
    public static final Profiler profiler = new ExempleProfiler();
    public static Config.ConfigContainer config;
    public static Data list;
    public static Config.Mode mode;
    public static List<Identifier> identifiers = new ArrayList<>();

    public static void load() {
        profiler.push("load");
        config.load();
        identifiers.clear();
        for (Config.Mode mode1 : config.get().modes) {
            identifiers.add(mode1.id);
        }
        {
            Identifier id = config.get().curent;
            for (Config.Mode mode1 : config.get().modes) if (mode1.id.equals(id)) {
                mode = mode1;
                break;
            }
        }
        try {
            list = (Data) Class.forName(mode.storage).getConstructor(Map.class).newInstance(mode.data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.log(Level.INFO, String.format("Load Plugin Configuration %sms", profiler.getElapsedTimeAndRemove(profiler.pop())));
    }

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new Event());
        this.getProxy().getPluginManager().registerCommand(this, new WhiteListCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
