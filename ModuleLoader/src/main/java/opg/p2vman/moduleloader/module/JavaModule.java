package opg.p2vman.moduleloader.module;

import opg.p2vman.moduleloader.ModuleLoader;
import opg.p2vman.moduleloader.api.Module;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class JavaModule implements Module {
    private boolean isEnabled = false;
    public static final List<JavaModule> MODULES = new ArrayList<>();
    private ModuleContainer container;
    private ModuleResourceMannager resourceMannager;
    private ModuleLogger logger;
    public JavaModule() {

    }


    public final void init(ModuleContainer container, ModuleResourceMannager resourceMannager, ModuleLogger logger) {
        for (JavaModule module : MODULES) if (this.getClass().isInstance(module)) {
            throw new RuntimeException();
        }
        this.resourceMannager = resourceMannager;
        this.container = container;
        this.logger = logger;
        MODULES.add(this);
    }

    public static <T extends JavaModule> T getModule(Class<T> cls) {
        for (JavaModule module : MODULES) if (cls.isInstance(module)) {
            return (T) module;
        }
        return null;
    }

    @Override
    public String getName() {
        return container.meta.name;
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onLoad() {

    }

    public void enable() {
        if (isEnabled) throw new RuntimeException();
        try {
            this.onEnable();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isEnabled = true;
        }
    }

    public void disable() {
        if (!isEnabled) throw new RuntimeException();
        try {
            this.onDisable();
            MODULES.remove(this);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isEnabled = false;
        }
    }

    @Override
    public ModuleResourceMannager getResourceMannager() {
        return resourceMannager;
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public ModuleLogger getLogger() {
        return logger;
    }

    public JavaPlugin getPlugin() {
        return ModuleLoader.getPlugin(ModuleLoader.class);
    }
}
