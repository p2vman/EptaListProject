package io.github.p2vman.lang;

import com.google.gson.JsonElement;

import java.io.InputStream;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface Lang extends BiConsumer<String, String>, Consumer<InputStream>, BiFunction<JsonElement, Stack<String>, Void> {
    Lang LANG = new Language();
    String get(String id);
    String getOrDefult(String id, String defult);
    default String getOrDefult(String id) {
        return getOrDefult(id, id);
    }
    boolean has(String id);
    void clear();
    String format(String id, Object... args);
    Formatter setFormater(Formatter formatter);
}
