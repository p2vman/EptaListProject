package io.github.p2vman;

import java.util.Map;

public class Utils {
    public static <T> boolean len(T[] t, int l) {
        return t.length > l;
    }

    public static <K, V> Map<K, V> put(Map<K, V> map, K k, V v) {
        map.put(k, v);
        return map;
    }
}
