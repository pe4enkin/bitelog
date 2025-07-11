package com.github.pe4enkin.bitelog.dao.exception;

public class ForeignKeyViolationException extends ConstraintViolationException {

    public ForeignKeyViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForeignKeyViolationException(String message) {
        super(message);
    }
}