package io.github.p2vman.nbt.tag;

import io.github.p2vman.nbt.TagReader;
import io.github.p2vman.nbt.TagWriter;
import io.github.p2vman.utils.Pair;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@ToString
public class TagCompound extends Tag {
    private Map<String, Tag> tagMap = new HashMap<>();
    public void put(String name, Tag tag) {
        tagMap.put(name, tag);
    }

    public Tag get(String name) {
        return tagMap.get(name);
    }

    public boolean is(String name) {
        return tagMap.containsKey(name);
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        for (Map.Entry<String, Tag> entry : tagMap.entrySet()) {
            writer.write(stream, entry.getValue(), entry.getKey());
        }
        stream.writeByte(0x00);
    }

    @Override
    public char getID() {
        return 10;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        this.tagMap.clear();
        while (true) {
            Pair<String, Tag> tagPair = reader.read(stream);
            if (tagPair.getV().getID() == 0) {
                break;
            }
            tagMap.put(tagPair.k, tagPair.v);
        }
    }
}
