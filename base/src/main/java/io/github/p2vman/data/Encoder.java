package io.github.p2vman.data;

public interface Encoder<O, T> {
    void encode(O out, T obj) throws EncoderException;
}
