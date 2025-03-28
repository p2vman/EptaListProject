package io.github.p2vman.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.p2vman.Static;
import lombok.ToString;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@ToString
public class Language implements Lang, BiConsumer<String, String>, Consumer<InputStream>, BiFunction<JsonElement, Stack<String>, Void> {
    private Formatter formatter = String::format;
    private Map<String, String> map;
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public Language() {
        map = new HashMap<>();
    }

    @Override
    public String get(String id) {
        return map.get(id);
    }

    @Override
    public String getOrDefult(String id) {
        return map.getOrDefault(id, id);
    }

    @Override
    public String getOrDefult(String id, String defult) {
        return map.getOrDefault(id, defult);
    }

    @Override
    public void accept(String s, String s2) {
        map.put(s, s2);
    }

    @Override
    public boolean has(String id) {
        return map.containsKey(id);
    }

    @Override
    public void accept(InputStream stream) {
        JsonObject jsonObject = Static.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            Stack<String> stack = new Stack<>();
            stack.push(entry.getKey());
            apply(entry.getValue(), stack);
        }
    }

    @Override
    public Void apply(JsonElement jsonElement, Stack<String> strings) {
        if (jsonElement.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                Stack<String> strings2 = new Stack<>();
                strings2.addAll(strings);
                strings2.push(entry.getKey());
                apply(entry.getValue(), strings2);
            }
        } else {
            this.accept(String.join(".", strings), UNSUPPORTED_FORMAT_PATTERN.matcher(jsonElement.getAsString()).replaceAll("%$1s"));
        }
        return null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public String format(String id, Object... args) {
        return formatter.format(getOrDefult(id), args);
    }

    public Formatter setFormater(Formatter formatter) {
        this.formatter = formatter;
        return formatter;
    }
}
