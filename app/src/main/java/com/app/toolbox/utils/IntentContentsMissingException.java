package com.app.toolbox.utils;

/**
 * {@code IntentContentsMissingException} is an exception that gets thrown
 * when some intent hasn't the required contents and method/receiver/service etc. can't work with it.
 * @see RuntimeException
 */

@SuppressWarnings("unused")
public class IntentContentsMissingException extends RuntimeException {
    public IntentContentsMissingException() {
        super();
    }

    public IntentContentsMissingException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntentContentsMissingException(Throwable cause) {
        super(cause);
    }

    protected IntentContentsMissingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IntentContentsMissingException(String message) {
        super(message);
    }
}
