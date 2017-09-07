package com.devinbrown.streaminglib;

/**
 * Created by devinbrown on 9/6/17.
 */

public class UnsupportedRtspMethodException extends RuntimeException {
    public UnsupportedRtspMethodException(String message) {
        super(message);
    }

    public UnsupportedRtspMethodException(String message, Throwable cause) {
        super(message, cause);
    }
}
