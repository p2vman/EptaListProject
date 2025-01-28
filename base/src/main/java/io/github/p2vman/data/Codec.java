package io.github.p2vman.data;

public interface Codec<T, I, O> extends Encoder<O, T>, Decoder<T, I> {
    static <T, I, O> Codec<T, I, O> of(final Encoder<O, T> encoder, final Decoder<T, I> decoder) {
        return new Codec<T, I, O>() {
            @Override
            public T decode(I input) throws DecoderException {
                return decoder.decode(input);
            }

            @Override
            public void encode(O out, T obj) throws EncoderException {
                encoder.encode(out, obj);
            }
        };
    }
}
