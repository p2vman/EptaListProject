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
public class TagDouble extends Tag {
    @Getter
    @Setter
    private double value;
    public TagDouble(double value) {
        this.value = value;
    }

    @Override
    public char getID() {
        return 6;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        byte[] buffer = new byte[Double.BYTES];
        stream.read(buffer);
        value = ByteBuffer.wrap(buffer).getDouble();
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.write(ByteBuffer.allocate(Double.BYTES).putDouble(value).array());
    }
}
