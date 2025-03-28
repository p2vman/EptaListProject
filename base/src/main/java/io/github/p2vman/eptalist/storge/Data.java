package io.github.p2vman.eptalist.storge;

import java.io.Closeable;
import java.util.List;

public interface Data<T> extends Closeable {
    boolean addUser(T name);
    boolean removeUser(T name);
    boolean is(T name);
    List<T> toList();
    boolean addUser(T name, List<String> info);
    boolean removeUser(T name, List<String> info);
    boolean is(T name, List<String> info);
    default boolean dirty() {
        return false;
    }
}
