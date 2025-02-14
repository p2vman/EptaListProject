package opg.p2vman.moduleloader.module;

import java.io.File;

public class ModuleContainer {
    public final File module_file;
    public final ModuleMeta meta;

    public ModuleContainer(ModuleMeta meta, File module_file) {
        this.meta = meta;
        this.module_file = module_file;
    }
}
