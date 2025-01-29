package io.github.p2vman.nbt.tag;

import io.github.p2vman.nbt.TagReader;
import io.github.p2vman.nbt.TagWriter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@NoArgsConstructor
@ToString
public class TagList extends Tag implements Iterable<Tag> {
    @Getter
    private char type;
    private List<Tag> values = new ArrayList<>(0);
    public int size() {
        return values.size();
    }

    public boolean add(Tag tag) {
        return values.add(tag);
    }

    public Tag remove(int index) {
        return values.remove(index);
    }

    public boolean remove(Tag tag) {
        return values.remove(tag);
    }

    public TagList(char type) {
        this.type = type;
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.writeByte(type);
        stream.writeInt(values.size());
        for (Tag tag : values) {
            tag.write(stream, writer);
        }
    }

    @Override
    public char getID() {
        return 9;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        this.values.clear();
        this.type = (char) stream.readByte();
        int s = stream.readInt();
        Class<? extends Tag> cls = reader.idclass.get(type);
        for (int i = 0; i < s; i++) {
            try {
                Tag tag = cls.getDeclaredConstructor().newInstance();
                tag.read(stream, reader);
                values.add(tag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Iterator<Tag> iterator() {
        return values.iterator();
    }
}
