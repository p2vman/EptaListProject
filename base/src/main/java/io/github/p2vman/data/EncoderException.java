package io.github.p2vman.data;

public class EncoderException extends Exception {
    public EncoderException() {
        super();
    }

    public EncoderException(final String message) {
        super(message);
    }

    public EncoderException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public EncoderException(final Throwable cause) {
        super(cause);
    }
}
