package io.github.p2vman.nbt;

import io.github.p2vman.nbt.tag.Tag;
import io.github.p2vman.nbt.tag.TagEnd;
import io.github.p2vman.utils.Pair;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

public class TagReader {
    public final Map<Character, Class<? extends Tag>> idclass;
    public TagReader(Map<Character, Class<? extends Tag>> idclass) {
        this.idclass = idclass;
    }

    public Pair<String, Tag> read(DataInputStream stream) throws IOException {
        char type = (char) stream.readByte();
        if (type == 0) {
            return new Pair<>("", new TagEnd());
        }

        for (Map.Entry<Character, Class<? extends Tag>> entry : idclass.entrySet()) {
            if (entry.getKey() == type) {
               try {
                   Pair<String, Tag> pair = new Pair<>(stream.readUTF(), entry.getValue().getDeclaredConstructor().newInstance());
                   pair.v.read(stream, this);
                   return pair;
               } catch (java.lang.InstantiationException | java.lang.IllegalAccessException | java.lang.NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
                   throw new IOException(e);
               }
            }
        }
        return new Pair<>("", new TagEnd());
    }
}
