package com.github.pe4enkin.bitelog.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private static String DB_FILE_PATH = "./data/bitelog.db";
    private static String DB_URL = "jdbc:sqlite:" + DB_FILE_PATH;
    private static final String DEFAULT_DB_FILE_PATH = "./data/bitelog.db";
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:" + DB_FILE_PATH;


    public static Connection getConnection() throws SQLException {
        File dbFile = new File(DB_FILE_PATH);
        boolean dbExists = dbFile.exists();
        if (!dbExists) {
            LOGGER.info("Файл базы данных не найден. Он будет создан по пути: {}", DB_FILE_PATH);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    LOGGER.debug("Создана директория для файла БД: {}", parentDir.getAbsolutePath());
                } else {
                    String errorMessage = "Не удалось создать директорию для файла БД: " +
                            parentDir.getAbsolutePath() + ". Проверьте права доступа.";
                    LOGGER.error(errorMessage);
                    throw new SQLException(errorMessage);
                }
            }
        } else {
            LOGGER.debug("Файл базы данных найден. Попытка подключения к существующей БД: {}", DB_FILE_PATH);
        }
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            if (!dbExists) {
                LOGGER.info("Новый файл базы данных успешно создан и соединение установлено.");
            } else {
                LOGGER.info("Соединение с существующей базой данных установлено.");
            }
            return connection;
        } catch (SQLException e) {
            LOGGER.error("Ошибка соединения с базой данных по пути: {}", DB_URL, e);
            throw e;
        }
    }

    public static void configureForTesting(String testFilePath) {
        LOGGER.debug("Настройка пути к базе данных для целей тестрирования: {}", testFilePath);
        DB_FILE_PATH = testFilePath;
        DB_URL = "jdbc:sqlite:" + DB_FILE_PATH;
    }

    public static void resetToDefault() {
        LOGGER.debug("Сброс пути к базе данных на значение по умолчанию: {}", DEFAULT_DB_FILE_PATH);
        DB_FILE_PATH = DEFAULT_DB_FILE_PATH;
        DB_URL = DEFAULT_DB_URL;
    }
}
