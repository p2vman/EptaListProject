package io.github.p2vman.eptalist.velocity;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.p2vman.Identifier;
import io.github.p2vman.eptalist.Config;
import io.github.p2vman.eptalist.Constants;
import io.github.p2vman.eptalist.metrics.SimplePie;
import io.github.p2vman.eptalist.storge.Data;
import io.github.p2vman.eptalist.storge.Storge;
import io.github.p2vman.eptalist.velocity.metrics.Metrics;
import io.github.p2vman.profiling.ExempleProfiler;
import io.github.p2vman.profiling.Profiler;
import io.github.p2vman.updater.Updater;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Plugin(id = "eptalist", name = "Eptalist", version = BuildConstants.VERSION, authors = "p2vman")
public class Velocity {
    private static Metrics metrics;
    public static final Profiler profiler = new ExempleProfiler();
    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("EptaList");
    @Inject
    private Logger logger;
    public static Config.ConfigContainer config;
    private Path dataDirectory;
    private final Metrics.Factory metricsFactory;

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

    @Inject
    public Velocity(ProxyServer server, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        profiler.push("init");
        this.metricsFactory = metricsFactory;
        this.dataDirectory = dataDirectory;
        if (!Files.exists(dataDirectory)) {
            dataDirectory.toFile().mkdirs();
        }
        config = new Config.ConfigContainer(new File(dataDirectory.toFile(), "config.cfg"));
        load();
        if (config.get().auto_update_check) {
            try {
                Updater updater = Updater.getInstance();
                JsonObject obj = updater.getLasted();
                if (!BuildConstants.VERSION.equals(obj.get("name").getAsString())) {
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

        CommandManager commandManager = server.getCommandManager();
        commandManager.register(config.get().command.getPath(), new WhiteListCommand(logger));
        LOGGER.log(Level.INFO, String.format("Load Plugin Configuration %sms", profiler.getElapsedTimeAndRemove(profiler.pop())));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Metrics metrics = metricsFactory.make(this, Constants.bstats_id);

        metrics.addCustomChart(new SimplePie("data_type", () -> mode.storage));
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (list.is(event.getPlayer().getUsername())) {
            event.setResult(ResultedEvent.ComponentResult.denied(Component.text(mode.kick_msg)));
        }
    }
}
