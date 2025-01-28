package io.github.p2vman.data;

@FunctionalInterface
public interface Decoder<R, I> {
    R decode(I input) throws DecoderException;
}
