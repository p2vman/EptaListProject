package io.github.p2vman.nbt.tag;

import io.github.p2vman.nbt.TagReader;
import io.github.p2vman.nbt.TagWriter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@NoArgsConstructor
@ToString
public class TagLongArray extends Tag {
    @Getter
    @Setter
    private long[] value;

    public int size() {
        if (value != null) return value.length;
        return 0;
    }
    public TagLongArray(long[] value) {
        this.value = value;
    }

    @Override
    public char getID() {
        return 12;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        value = new long[stream.readInt()];
        for (int i = 0; i < value.length; i++) {
            value[i] = stream.readLong();
        }
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.writeInt(value.length);
        for (int i = 0; i < value.length; i++) {
            stream.writeLong(value[i]);
        }
    }
}
