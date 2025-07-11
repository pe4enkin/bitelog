package com.github.pe4enkin.bitelog.dao.exception;

public class ConstraintViolationException extends DataAccessException {

    public ConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConstraintViolationException(String message) {
        super(message);
    }
}