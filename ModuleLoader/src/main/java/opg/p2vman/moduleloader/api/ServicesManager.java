package opg.p2vman.moduleloader.api;

import java.util.HashMap;
import java.util.Map;

public class ServicesManager {

    private final Map<Class<?>, Object> services = new HashMap<>();

    public <T> void registerService(Class<T> serviceClass, T serviceInstance) {
        services.put(serviceClass, serviceInstance);
    }

    public <T> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }

    public boolean hasService(Class<?> serviceClass) {
        return services.containsKey(serviceClass);
    }
}
