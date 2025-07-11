package com.github.pe4enkin.bitelog.dao.exception;

public class DuplicateKeyException extends ConstraintViolationException {

    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateKeyException(String message) {
        super(message);
    }
}