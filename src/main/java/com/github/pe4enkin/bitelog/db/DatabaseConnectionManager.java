package com.github.pe4enkin.bitelog.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private static final String DB_FILE_PATH = "./data/bitelog.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE_PATH;

    public static Connection getConnection() throws SQLException {
        File dbFile = new File(DB_FILE_PATH);
        boolean dbExists = dbFile.exists();
        if (!dbExists) {
            logger.info("Файл базы данных не найден. Он будет создан по пути: {}", DB_FILE_PATH);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    logger.debug("Создана директория для файла БД: {}", parentDir.getAbsolutePath());
                } else {
                    String errorMessage = "Не удалось создать директорию для файла БД: " +
                            parentDir.getAbsolutePath() + ". Проверьте права доступа.";
                    logger.error(errorMessage);
                    throw new SQLException(errorMessage);
                }
            }
        } else {
            logger.debug("Файл базы данных найден. Попытка подключения к существующей БД: {}", DB_FILE_PATH);
        }
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            if (!dbExists) {
                logger.info("Новый файл базы данных успешно создан и соединение установлено.");
            } else {
                logger.info("Соединение с существующей базой данных установлено.");
            }
            return connection;
        } catch (SQLException e) {
            logger.error("Ошибка соединения с базой данных по пути: {}", DB_URL, e);
            throw e;
        }
    }
}
