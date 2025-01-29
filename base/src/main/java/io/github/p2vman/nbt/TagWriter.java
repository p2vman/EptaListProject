package io.github.p2vman.nbt;

import io.github.p2vman.nbt.tag.Tag;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class TagWriter {
    public final Map<Character, Class<? extends Tag>> idclass;
    public TagWriter(Map<Character, Class<? extends Tag>> idclass) {
        this.idclass = idclass;
    }

    public void write(DataOutputStream stream, Tag tag) throws IOException {
        write(stream, tag, "");
    }

    public void write(DataOutputStream stream, Tag tag, String name) throws IOException {
        for (Map.Entry<Character, Class<? extends Tag>> entry : idclass.entrySet()) {
            if (entry.getValue() == tag.getClass()) {
                stream.write(tag.getID());
                stream.writeUTF(name);
                tag.write(stream, this);
            }
        }
    }
}
