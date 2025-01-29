package io.github.p2vman.nbt;

import io.github.p2vman.nbt.tag.*;
import io.github.p2vman.utils.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NbtIo {
    private Map<Character, Class<? extends Tag>> idclass;

    public NbtIo(Map<Character, Class<? extends Tag>> idclass) {
        this.idclass = idclass;
    }

    public Serializer getSerializer() {
        return new Serializer(this);
    }

    public NbtIo() {
        this.idclass = new HashMap<>();
        for (Tag tag : new Tag[] {
                new TagEnd(),
                new TagFloat(),
                new TagInt(),
                new TagCompound(),
                new TagIntArray(),
                new TagList(),
                new TagByte(),
                new TagByteArray(),
                new TagDouble(),
                new TagLong(),
                new TagLongArray(),
                new TagShort(),
                new TagString()
        }) {
            idclass.put(tag.getID(), tag.getClass());
        }
    }

    public Pair<String, Tag> read(DataInputStream stream) throws IOException {
        TagReader reader = new TagReader(this.idclass);
        return reader.read(stream);
    }

    public void write(DataOutputStream stream, Tag tag) throws IOException {
        TagWriter writer = new TagWriter(this.idclass);
        writer.write(stream, tag);
    }

    public TagCompound readCompound(DataInputStream stream) throws IOException {
        TagReader reader = new TagReader(this.idclass);
        Tag tag = reader.read(stream).v;
        return tag instanceof TagCompound ? (TagCompound) tag : new TagCompound();
    }

    public static NbtIo create(Tag... tags) {
        return new NbtIo();
    }
}
