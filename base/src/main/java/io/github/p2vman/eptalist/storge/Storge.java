package io.github.p2vman.eptalist.storge;

import io.github.p2vman.Utils;
import j.ApiStatus;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.In_Development
public abstract class Storge<T> implements Closeable {

    public static final Map<String, Class<? extends Data<String>>> sm = Utils.wappeler(new HashMap<String, Class<? extends Data<String>>>())
            .put("NBT", NBT.class)
            .put("nbt", NBT.class)
            .put("JSON", Json.class)
            .put("json", Json.class)
            .put("Sqlite", Sqlite.class)
            .put("SQLITE", Sqlite.class)
            .put("mysql", Mysql.class)
            .put("MYSQL", Mysql.class)
            .put("org.eptalist.storge.Json", Json.class)
            .put("org.eptalist.storge.NBT", NBT.class)
            .put("org.eptalist.storge.Sqlite", Sqlite.class)
            .put("org.eptalist.storge.Mysql", Mysql.class)
            .getMap();

    public static Class<? extends Data<String>> find(String clz) throws ClassNotFoundException, ClassCastException {
        Class<?> cls = null;
        if (sm.containsKey(clz)) {
            cls = sm.get(clz);
        }

        try {
            cls = Class.forName(clz);
        } catch (Exception e) {

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
    public abstract Answer addUser(T name);
    public abstract Answer removeUser(T name);
    public abstract Answer is(T name);

    @ApiStatus.In_Development
    public class Answer {

    }
}
