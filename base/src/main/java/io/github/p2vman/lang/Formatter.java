package io.github.p2vman.lang;

@FunctionalInterface
public interface Formatter {
    String format(String string, Object... args);
}
