package com.app.toolbox.utils;

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
