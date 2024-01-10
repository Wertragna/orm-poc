package org.essential.core.exceptions;

public class OrmException extends RuntimeException {
    public OrmException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrmException() {}
}
