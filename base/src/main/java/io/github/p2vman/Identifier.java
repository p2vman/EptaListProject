package io.github.p2vman;

public class Identifier implements Comparable<Identifier> {
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    private final String namespace;
    private final String path;
    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }
    private Identifier(String[] id) {
        this(id[0], id[1]);
    }

    public Identifier(String id) {
        this(split(id, NAMESPACE_SEPARATOR));
    }

    public static Identifier splitOn(String id, char delimiter) {
        return new Identifier(split(id, delimiter));
    }

    public static Identifier tryParse(String id) {
        try {
            return new Identifier(id);
        } catch (RuntimeException var2) {
            return null;
        }
    }

    public static Identifier of(String namespace, String path) {
        try {
            return new Identifier(namespace, path);
        } catch (RuntimeException var3) {
            return null;
        }
    }

    protected static String[] split(String id, char delimiter) {
        String[] strings = new String[]{DEFAULT_NAMESPACE, id};
        int i = id.indexOf(delimiter);
        if (i >= 0) {
            strings[1] = id.substring(i + 1);
            if (i >= 1) {
                strings[0] = id.substring(0, i);
            }
        }

        return strings;
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String toString() {
        return this.namespace + NAMESPACE_SEPARATOR + this.path;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Identifier)) {
            return false;
        } else {
            Identifier identifier = (Identifier)o;
            return this.namespace.equals(identifier.namespace) && this.path.equals(identifier.path);
        }
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    public int compareTo(Identifier identifier) {
        int i = this.path.compareTo(identifier.path);
        if (i == 0) {
            i = this.namespace.compareTo(identifier.namespace);
        }

        return i;
    }

    public String toUnderscoreSeparatedString() {
        return this.toString().replace('/', '_').replace(NAMESPACE_SEPARATOR, '_');
    }

    public String toTranslationKey() {
        return this.namespace + "." + this.path;
    }

    public String toShortTranslationKey() {
        return this.namespace.equals(DEFAULT_NAMESPACE) ? this.path : this.toTranslationKey();
    }

    public String toTranslationKey(String prefix) {
        return prefix + "." + this.toTranslationKey();
    }

    public String toTranslationKey(String prefix, String suffix) {
        return prefix + "." + this.toTranslationKey() + "." + suffix;
    }

    public static boolean isCharValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
    }

    public static boolean isPathValid(String path) {
        for(int i = 0; i < path.length(); ++i) {
            if (!isPathCharacterValid(path.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isNamespaceValid(String namespace) {
        for(int i = 0; i < namespace.length(); ++i) {
            if (!isNamespaceCharacterValid(namespace.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static String validateNamespace(String namespace, String path) {
        if (!isNamespaceValid(namespace)) {
            throw new RuntimeException("Non [a-z0-9_.-] character in namespace of location: " + namespace + NAMESPACE_SEPARATOR + path);
        } else {
            return namespace;
        }
    }

    public static boolean isPathCharacterValid(char character) {
        return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '/' || character == '.';
    }

    private static boolean isNamespaceCharacterValid(char character) {
        return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '.';
    }

    private static String validatePath(String namespace, String path) {
        if (!isPathValid(path)) {
            throw new RuntimeException("Non [a-z0-9/._-] character in path of location: " + namespace + NAMESPACE_SEPARATOR + path);
        } else {
            return path;
        }
    }
}