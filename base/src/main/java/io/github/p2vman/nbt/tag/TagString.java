package io.github.p2vman.nbt.tag;

import io.github.p2vman.nbt.TagWriter;
import io.github.p2vman.nbt.TagReader;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@NoArgsConstructor
@ToString
public class TagString extends Tag {
    @Getter
    @Setter
    private String value;
    public TagString(String value) {
        this.value = value;
    }

    @Override
    public char getID() {
        return 8;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        value = stream.readUTF();
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.writeUTF(value);
    }
}