package opg.p2vman.moduleloader.api;

import java.io.InputStream;
import java.net.URL;

public interface ResourceMannager {
    InputStream getResourceAsStream(String name);
    URL getResource(String name);
    InputStream getResourceAsStream(Identifier identifier);
    URL getResource(Identifier identifier);
    URL findResource(final String name);
    URL findResource(final Identifier identifier);
}
