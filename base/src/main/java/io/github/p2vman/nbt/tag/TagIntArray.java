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
import java.nio.ByteBuffer;

@NoArgsConstructor
@ToString
public class TagIntArray extends Tag {
    @Getter
    @Setter
    private int[] value;

    public int size() {
        if (value != null) return value.length;
        return 0;
    }
    public TagIntArray(int[] value) {
        this.value = value;
    }

    @Override
    public char getID() {
        return 11;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        byte[] buffer = new byte[4];
        stream.read(buffer);
        value = new int[ByteBuffer.wrap(buffer).getInt()];
        for (int i = 0; i < value.length; i++) {
            stream.read(buffer);
            value[i] = ByteBuffer.wrap(buffer).getInt();
        }
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.write(ByteBuffer.allocate(Integer.BYTES).putInt(value.length).array());
        for (int i = 0; i < value.length; i++) {
            stream.write(ByteBuffer.allocate(Integer.BYTES).putInt(value[i]).array());
        }
    }
}
