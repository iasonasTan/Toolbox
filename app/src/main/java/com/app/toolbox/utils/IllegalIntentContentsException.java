package com.app.toolbox.utils;

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
