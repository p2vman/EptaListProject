package opg.p2vman.moduleloader.module;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ModuleLogger extends Logger {
    private String name;

    /**
     * Creates a new ModuleLogger that extracts the name from a module.
     *
     * @param context A reference to the module
     */
    public ModuleLogger(@NotNull ModuleContainer context) {
        super(context.getClass().getCanonicalName(), null);
        name = new StringBuilder().append("[").append(context.meta.name).append("] ").toString();
        setParent(Bukkit.getServer().getLogger());
        setLevel(Level.ALL);
    }

    @Override
    public void log(@NotNull LogRecord logRecord) {
        logRecord.setMessage(name + logRecord.getMessage());
        super.log(logRecord);
    }
}