package org.eptalist.spigot;

import com.google.gson.JsonObject;
import io.github.p2vman.profiling.ExempleProfiler;
import io.github.p2vman.profiling.Profiler;
import io.github.p2vman.updater.Updater;
import org.eptalist.Config;
import io.github.p2vman.Identifier;
import org.eptalist.Constants;
import org.eptalist.metrics.SimplePie;
import org.eptalist.spigot.metrics.Metrics;
import org.eptalist.storge.Data;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EptaList extends JavaPlugin {
    private static Metrics metrics;
    public static final Profiler profiler = new ExempleProfiler();
    public static final Logger LOGGER = Logger.getLogger("EptaList");

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


        profiler.push("init");
        metrics = new Metrics(this, Constants.bstats_id);

        File data = getDataFolder();
        if (!data.exists()) {
            data.mkdirs();
        }
        config = new Config.ConfigContainer(new File(data, "wh.json"));
        load();

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap map = (CommandMap)commandMapField.get(Bukkit.getServer());

            Command command = new WhiteListCommand();

            map.register("minecraft", command);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        getServer().getPluginManager().registerEvents(new Event(), this);
        metrics.addCustomChart(new SimplePie("data_type", () -> mode.storage));
        LOGGER.log(Level.INFO, String.format("Init Plugin %sms", profiler.getElapsedTimeAndRemove(profiler.pop())));
    }

    @Override
    public void onDisable() {
        try {
            if (list != null) list.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
