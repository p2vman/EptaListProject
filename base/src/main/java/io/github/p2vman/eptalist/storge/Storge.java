package io.github.p2vman.eptalist.storge;

import io.github.p2vman.Utils;
import j.ApiStatus;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@ApiStatus.In_Development
public abstract class Storge<T> implements Closeable {
    public static ExecutorService threadPool = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        private int count = 0;

        @Override
        public Thread newThread(Runnable r) {
            Thread t = defaultFactory.newThread(r);
            t.setName("data-async-pool-" + count++);
            t.setDaemon(true);
            return t;
        }
    });

    public static final Map<String, Class<? extends Data<String>>> sm;

    static {
        sm = new HashMap<>();
        sm.put("NBT", NBT.class);
        sm.put("nbt", NBT.class);
        sm.put("JSON", Json.class);
        sm.put("json", Json.class);
        sm.put("Sqlite", Sqlite.class);
        sm.put("SQLITE", Sqlite.class);
        sm.put("mysql", Mysql.class);
        sm.put("MYSQL", Mysql.class);
        sm.put("org.eptalist.storge.Json", Json.class);
        sm.put("org.eptalist.storge.NBT", NBT.class);
        sm.put("org.eptalist.storge.Sqlite", Sqlite.class);
        sm.put("org.eptalist.storge.Mysql", Mysql.class);
    }

    public static Class<? extends Data<String>> find(String clz) throws ClassNotFoundException, ClassCastException {
        Class<?> cls = null;
        if (sm.containsKey(clz)) {
            cls = sm.get(clz);
        } else {
            try {
                cls = Class.forName(clz);
            } catch (Exception e) {

            }
        }

        if (cls == null) throw new ClassNotFoundException();
        if (!Data.class.isAssignableFrom(cls)) throw new ClassCastException();
        return (Class<? extends Data<String>>) cls;
    }

    public boolean dirty() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }

    public abstract List<T> toList();
    public abstract Answer addUserAsync(T name);
    public abstract Answer addUser(T name);
    public abstract Answer removeUser(T name);
    public abstract Answer removeUserAsync(T name);
    public abstract Answer isAsync(T name);

    @ApiStatus.In_Development
    public static class Answer {

    }
}
