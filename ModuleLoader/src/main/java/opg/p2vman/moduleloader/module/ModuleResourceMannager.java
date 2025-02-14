package opg.p2vman.moduleloader.module;

import opg.p2vman.moduleloader.api.Identifier;
import opg.p2vman.moduleloader.api.ResourceMannager;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ModuleResourceMannager extends URLClassLoader implements ResourceMannager {
    private ModuleContainer container;
    public ModuleResourceMannager(ModuleContainer container) throws MalformedURLException {
        super(new URL[]{container.module_file.toURI().toURL()});
        this.container = container;
    }
    @Override
    public URL getResource(Identifier identifier) {
        return getResource(String.format("assets/%s/%s", identifier.getNamespace(), identifier.getPath()));
    }

    @Override
    public InputStream getResourceAsStream(Identifier identifier) {
        return getResourceAsStream(String.format("assets/%s/%s", identifier.getNamespace(), identifier.getPath()));
    }

    @Override
    public URL findResource(Identifier identifier) {
        return findResource(String.format("assets/%s/%s", identifier.getNamespace(), identifier.getPath()));
    }
}
