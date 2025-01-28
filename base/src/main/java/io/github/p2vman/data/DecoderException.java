package io.github.p2vman.data;

public class DecoderException extends Exception {
    public DecoderException() {
        super();
    }
    public DecoderException(final String message) {
        super(message);
    }
    public DecoderException(final String message, final Throwable cause) {
        super(message, cause);
    }
    public DecoderException(final Throwable cause) {
        super(cause);
    }
}
