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
public class TagLong extends Tag {
    @Getter
    @Setter
    private long value;
    public TagLong(long value) {
        this.value = value;
    }

    @Override
    public char getID() {
        return 4;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        value = stream.readLong();
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.writeLong(value);
    }
}
