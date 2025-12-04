package com.app.toolbox.utils;

/**
 * {@code IllegalIntentContentsException} is an exception that gets thrown
 * when some intent has illegal contents and method/receiver/service etc. can't work with it.
 * @see RuntimeException
 */

@SuppressWarnings("unused")
public class IllegalIntentContentsException extends RuntimeException {
    public IllegalIntentContentsException() {
        super();
    }

    public IllegalIntentContentsException(String message) {
        super(message);
    }

    public IllegalIntentContentsException(String message, Throwable cause) {
        super(message, cause);
    }

    protected IllegalIntentContentsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IllegalIntentContentsException(Throwable cause) {
        super(cause);
    }
}
