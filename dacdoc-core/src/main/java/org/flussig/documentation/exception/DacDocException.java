package org.flussig.documentation.exception;

/**
 * Specific exception for DacDoc processing
 */
public class DacDocException extends Exception {
    public DacDocException(String message) {
        super(message);
    }
    public DacDocException(String message, Throwable cause) {
        super(message, cause);
    }
}
