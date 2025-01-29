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
public class TagFloat extends Tag {
    @Getter
    @Setter
    private float value;
    public TagFloat(float value) {
        this.value = value;
    }

    @Override
    public char getID() {
        return 5;
    }

    @Override
    public void read(DataInputStream stream, TagReader reader) throws IOException {
        byte[] buffer = new byte[Float.BYTES];
        stream.read(buffer);
        value = ByteBuffer.wrap(buffer).getFloat();
    }

    @Override
    public void write(DataOutputStream stream, TagWriter writer) throws IOException {
        stream.write(ByteBuffer.allocate(Float.BYTES).putFloat(value).array());
    }
}
