package opg.p2vman.moduleloader;

import opg.p2vman.moduleloader.module.JavaModule;
import opg.p2vman.moduleloader.module.ModuleMannager;
import opg.p2vman.moduleloader.profiling.ExempleProfiler;
import opg.p2vman.moduleloader.profiling.Profiler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Optional;

public final class ModuleLoader extends JavaPlugin {
    private Optional<ModuleMannager> loader = Optional.empty();
    @Override
    public void onEnable() {
        Profiler profiler = new ExempleProfiler();
        profiler.push("start_module_loader");
        try {
            ModuleMannager moduleLoader = new ModuleMannager();
            File modules_dir = new File(getDataFolder(), "modules");
            if (!modules_dir.exists()) {
                modules_dir.mkdirs();
            }

            moduleLoader.loadModules(modules_dir).forEach((javaModule -> {
                javaModule.enable();
                System.out.printf("Module enabled %s", javaModule.getName());
            }));

            loader = Optional.of(moduleLoader);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.printf("modules loaded %dms%n", profiler.getElapsedTimeAndRemove(profiler.pop()));
    }

    @Override
    public void onDisable() {
        JavaModule.MODULES.forEach(JavaModule::disable);
    }

    public Optional<ModuleMannager> getModuleLoader() {
        return loader;
    }
}
