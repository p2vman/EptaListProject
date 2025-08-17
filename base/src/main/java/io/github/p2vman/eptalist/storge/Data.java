package io.github.p2vman.eptalist.storge;


import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public interface Data<T> extends Closeable {
    boolean addUser(T name);
    boolean removeUser(T name);
    boolean is(T name);
    List<T> toList();
    boolean addUser(T name, Consumer<String> callback);
    boolean removeUser(T name, Consumer<String> callback);
    boolean is(T name, Consumer<String> callback);
    default boolean dirty() {
        return false;
    }

    default CompletionStage<Boolean> addUserAsync(T name, Consumer<String> callback) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                return addUser(name, callback);
            }
        }, Storge.threadPool);
    }

    default CompletableFuture<Boolean> removeUserAsync(T name, Consumer<String> callback) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                return removeUser(name, callback);
            }
        }, Storge.threadPool);
    }

    default CompletableFuture<Boolean> isAsync(T name, Consumer<String> callback) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                return is(name, callback);
            }
        }, Storge.threadPool);
    }
}
