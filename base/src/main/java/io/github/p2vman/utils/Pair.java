package io.github.p2vman.utils;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Pair<K, V> {
    public K k;
    public V v;
}
