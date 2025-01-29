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
public class TagInt extends Tag {
    @Getter
    @Setter
    private int value;
    public TagInt(int value) {
        this.value = value;
    }

    @Override
    public char getID() {
        return 3;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        byte[] buffer = new byte[Integer.BYTES];
        stream.read(buffer);
        value = ByteBuffer.wrap(buffer).getInt();
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.write(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
    }
}
