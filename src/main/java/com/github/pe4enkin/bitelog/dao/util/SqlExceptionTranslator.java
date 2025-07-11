package com.github.pe4enkin.bitelog.dao.util;

import com.github.pe4enkin.bitelog.dao.exception.ConstraintViolationException;
import com.github.pe4enkin.bitelog.dao.exception.DataAccessException;
import com.github.pe4enkin.bitelog.dao.exception.DuplicateKeyException;
import com.github.pe4enkin.bitelog.dao.exception.ForeignKeyViolationException;

import java.sql.SQLException;

public final class SqlExceptionTranslator {

    private SqlExceptionTranslator() {
    }

    public static DataAccessException translate(SQLException e, String operationDescription) {
        int errorCode = e.getErrorCode();
        String sqlState = e.getSQLState();
        String message = e.getMessage();

        if (errorCode == 19) {
            if (message != null) {
                if (message.contains("FOREIGN KEY constraint failed")) {
                    return new ForeignKeyViolationException("Нарушение внешнего ключа при " + operationDescription, e);
                } else if (message.contains("UNIQUE constraint failed")) {
                    return new DuplicateKeyException("Нарушение уникального ограничения при " + operationDescription, e);
                } else if (message.contains("NOT NULL constraint failed")) {
                    return new ConstraintViolationException("Нарушение NOT NULL ограничения при " + operationDescription, e);
                } else if (message.contains("CHECK constraint failed")) {
                    return new ConstraintViolationException("Нарушение CHECK ограничения при " + operationDescription, e);
                }
            }
            return new ConstraintViolationException("Нарушение ограничения целостности при " + operationDescription, e);
        }
        return new DataAccessException("Общая ошибка БД при " + operationDescription, e);
    }
}