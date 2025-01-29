package io.github.p2vman.nbt.tag;

import io.github.p2vman.nbt.TagReader;
import io.github.p2vman.nbt.TagWriter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@NoArgsConstructor
@ToString
public class TagEnd extends Tag {
    @Override
    public char getID() {
        return 0;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {

    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.write(0x00);
    }
}
