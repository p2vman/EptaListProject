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
public class TagByte extends Tag {
    @Getter
    @Setter
    private byte value;
    public TagByte(byte value) {
        this.value = value;
    }

    @Override
    public char getID() {
        return 1;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        byte[] buffer = new byte[1];
        stream.read(buffer);
        value = buffer[0];
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.write(value);
    }
}
