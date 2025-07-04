package com.github.pe4enkin.bitelog.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private static volatile String dbFilePath = "./data/bitelog.db";
    private static final String DEFAULT_DB_FILE_PATH = "./data/bitelog.db";

    private static DataSource dataSourceInstance;

    public static DataSource getDataSource() throws SQLException {
        if (dataSourceInstance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (dataSourceInstance == null) {
                    dataSourceInstance = createDataSource(dbFilePath);
                    LOGGER.info("Инициализация DataSource для пути: {}", dbFilePath);
                }
            }
        }
        return dataSourceInstance;
    }

    private static DataSource createDataSource(String path)  throws SQLException {
        SQLiteDataSource sqLiteDataSource = new SQLiteDataSource();
        String url = "jdbc:sqlite:" + path;
        sqLiteDataSource.setUrl(url);

        if (url.contains("mode=memory")) {
            SQLiteConfig config = new SQLiteConfig();
            config.setSharedCache(true);
            sqLiteDataSource.setConfig(config);
            LOGGER.debug("Конфигурация БД memory с общим кэшем: {}", url);
        } else {
            File dbFile = new File(path);
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
            LOGGER.debug("Конфигурация БД file-based: {}", url);
        }

        try (Connection connection = sqLiteDataSource.getConnection()) {
            LOGGER.info("Соединение с базой данных установлено: {}", url);
        } catch (SQLException e) {
            LOGGER.error("Ошибка соединения с базой данных: {}", url, e);
            throw e;
        }
        return sqLiteDataSource;
    }

    public static void configureForTesting(String testFilePath) {
        LOGGER.debug("Настройка пути к базе данных для целей тестрирования: {}", testFilePath);
        dbFilePath = testFilePath;
        dataSourceInstance = null;
    }

    public static void resetToDefault() {
        LOGGER.debug("Сброс пути к базе данных на значение по умолчанию: {}", DEFAULT_DB_FILE_PATH);
        dbFilePath = DEFAULT_DB_FILE_PATH;
        dataSourceInstance = null;
    }

    public static void closeDataSource() {
        if (dataSourceInstance instanceof AutoCloseable closeable) {
            try {
                closeable.close();
                LOGGER.info("DataSource успешно закрыт.");
            } catch (Exception e) {
                LOGGER.info("Ошибка закрытия DataSource.", e);
            }
            dataSourceInstance = null;
        } else if (dataSourceInstance != null) {
            LOGGER.warn("DataSource не является AutoCloseable, попытка явного закрытия невозможна.");
            dataSourceInstance = null;
        }
    }
}