package io.github.p2vman.eptalist.bounge;

import com.google.gson.JsonObject;
import io.github.p2vman.Identifier;
import io.github.p2vman.eptalist.storge.Storge;
import io.github.p2vman.profiling.ExempleProfiler;
import io.github.p2vman.profiling.Profiler;
import io.github.p2vman.updater.Updater;
import net.md_5.bungee.api.plugin.Plugin;
import io.github.p2vman.eptalist.Config;
import io.github.p2vman.eptalist.Constants;
import io.github.p2vman.eptalist.bounge.metrics.Metrics;
import io.github.p2vman.eptalist.metrics.SimplePie;
import io.github.p2vman.eptalist.storge.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class Boungecord extends Plugin {
    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("EptaList");
    public static final Profiler profiler = new ExempleProfiler();
    public static Config.ConfigContainer config;
    public static Data<String> list;
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
            list = Storge.find(mode.storage).getConstructor(Map.class).newInstance(mode.data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.log(Level.INFO, String.format("Load Plugin Configuration %sms", profiler.getElapsedTimeAndRemove(profiler.pop())));
    }

    @Override
    public void onEnable() {
        File data = getDataFolder();
        if (!data.exists()) {
            data.mkdirs();
        }
        config = new Config.ConfigContainer(new File(data, "config.cfg"));
        load();
        if (config.get().auto_update_check)
        {
            try {
                Updater updater = Updater.getInstance();
                JsonObject obj = updater.getLasted();
                if (!getDescription().getVersion().equals(obj.get("name").getAsString())) {
                    LOGGER.log(Level.WARNING, "---------- Outdated Version ----------");
                    LOGGER.log(Level.WARNING, "");
                    LOGGER.log(Level.WARNING, "new version:");
                    LOGGER.log(Level.WARNING, updater.getVersionUrl());
                    LOGGER.log(Level.WARNING, "");
                    LOGGER.log(Level.WARNING, "---------------------------------");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Metrics metrics = new Metrics(this, Constants.bstats_id);

        metrics.addCustomChart(new SimplePie("data_type", () -> mode.storage));
        getProxy().getPluginManager().registerListener(this, new Event());
        this.getProxy().getPluginManager().registerCommand(this, new WhiteListCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
