package com.github.pe4enkin.bitelog.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionManagerTest {
    @TempDir
    Path tempDir;

    private String testDbFilePath;

    @BeforeEach
    void setUp() {
        Path testDataDir = tempDir.resolve("data");
        testDbFilePath = testDataDir.resolve("bitelog_test.db").toString();
        DatabaseConnectionManager.configureForTesting(testDbFilePath);
    }

    @AfterEach
    void tearDown() {
        DatabaseConnectionManager.resetToDefault();
        DatabaseConnectionManager.closeDataSource();
    }

    @Test
    @DisplayName("Создание директории data и файла базы данных, если их не существует.")
    void shouldCreateDataDirectoryAndDbFileIfNotExists() throws SQLException {
        File dbFile = new File(testDbFilePath);
        File parentDir = dbFile.getParentFile();

        assertFalse(parentDir.exists(), "Директории data не должно существовать перед тестом.");
        assertFalse(dbFile.exists(), "Файла базы данных не должно существовать перед тестом.");

        DataSource dataSource = DatabaseConnectionManager.getDataSource();
        assertNotNull(dataSource);
        assertTrue(parentDir.exists(), "Директория data должна создаться.");
        assertTrue(dbFile.exists(), "Файл базы данных должен быть создан.");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
        }
    }

    @Test
    @DisplayName("Подключение к существующей базе данных")
    void shouldConnectToExistingDbFile() throws SQLException, IOException {
        File dbFile = new File(testDbFilePath);
        File parentDir = dbFile.getParentFile();

        parentDir.mkdirs();
        Files.createFile(dbFile.toPath());
        assertTrue(dbFile.exists(), "Файл базы данных должен существовать перед тестом.");

        DataSource dataSource = DatabaseConnectionManager.getDataSource();
        assertNotNull(dataSource, "DataSource не должен быть null.");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed(), "Соединение не должно быть закрыто.");
        }
    }

    @Test
    @DisplayName("SQLException при ошибке создании директории data.")
    void shouldThrowSQLExceptionIfDirectoryCreationFails() throws IOException {
        Path blockingPath = tempDir.resolve("this_is_a_file_not_a_directory");
        Files.createFile(blockingPath);
        String inaccessiblePath = blockingPath.resolve("sub_dir").resolve("test_db.db").toString();
        DatabaseConnectionManager.configureForTesting(inaccessiblePath);

        SQLException thrown = assertThrows(SQLException.class, () -> {
            DatabaseConnectionManager.getDataSource();
        }, "Должно быть SQLException при ошибке создании директории data");
        assertTrue(thrown.getMessage().contains("Не удалось создать директорию для файла БД"),
                "Сообщение об ошибке должно указывать на проблему с созданием директории.");
    }

    @Test
    @DisplayName("Должен сбросить DataSource к значению по умолчанию и закрыть старый.")
    void shouldResetToDefaultAndCloseOldDataSource() throws SQLException {
        DataSource initialDataSource = DatabaseConnectionManager.getDataSource();
        assertNotNull(initialDataSource);

        DatabaseConnectionManager.resetToDefault();

        DataSource newDataSource = DatabaseConnectionManager.getDataSource();
        assertNotNull(newDataSource);
        assertNotSame(initialDataSource, newDataSource, "После сброса DataSource должен быть новым экземпляром.");

        try (Connection connection = newDataSource.getConnection()) {
            assertNotNull(connection);
        }
    }
}