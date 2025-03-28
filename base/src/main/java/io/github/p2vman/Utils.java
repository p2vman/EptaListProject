package io.github.p2vman;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

public class Utils {
    public static <T> boolean len(T[] t, int l) {
        return t.length > l;
    }

    public static <K, V> MapWappeler<K, V> wappeler(Map<K, V> map) {
        return new MapWappeler<>(map);
    }

    @Getter
    @AllArgsConstructor
    public static class MapWappeler<K, V> {
        public final Map<K, V> map;

        public MapWappeler<K, V> put(K k, V v) {
            map.put(k, v);
            return this;
        }
    }

    public static <K, V> Map<K, V> put(Map<K, V> map, K k, V v) {
        map.put(k, v);
        return map;
    }
}
