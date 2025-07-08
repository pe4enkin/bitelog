package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import com.github.pe4enkin.bitelog.sql.SqlQueries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FoodCategoryDaoTest {
    private FoodCategoryDao foodCategoryDao;
    private DataSource testDataSource;
    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnectionManager.configureForTesting("file:memdb1?mode=memory&cache=shared");
        testDataSource = DatabaseConnectionManager.getDataSource();
        testConnection  = testDataSource.getConnection();
        foodCategoryDao = new FoodCategoryDao(testDataSource);
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(SqlQueries.DROP_TABLE_FOOD_CATEGORIES);
        }
        foodCategoryDao.createTables();
        try (PreparedStatement pstmt = testConnection.prepareStatement(SqlQueries.SELECT_TABLE_NAME)) {
            pstmt.setString(1, "food_categories");
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Таблица food_categories должна существовать после createTables.");
                assertEquals("food_categories", rs.getString("name"), "Имя найденной таблицы должно совпадать с food_categories.");
            }
        };
    }

    @AfterEach
    void tearDown() throws SQLException{
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
        DatabaseConnectionManager.resetToDefault();
        DatabaseConnectionManager.closeDataSource();
    }

    @Test
    @DisplayName("Метод save должен сохранить FoodCategory и сгенерировать ID.")
    void save_shouldSaveFoodCategory() throws SQLException {
        FoodCategory category = new FoodCategory("Мясо");
        FoodCategory savedCategory = foodCategoryDao.save(category);

        assertNotNull(savedCategory, "Сохраненный FoodCategory должен быть не null.");
        assertTrue(savedCategory.getId() > 0, "ID сохраненного FoodCategory должен быть сгенерирован и быть больше 0.");
        assertEquals("Мясо", savedCategory.getName(), "Имя FoodCategory должно совпадать.");
    }

    @Test
    @DisplayName("Метод findById должен найти существующую FoodCategory по ID.")
    void findById_shouldReturnExistingFoodCategory() throws SQLException {
        FoodCategory category = new FoodCategory("Мясо");
        FoodCategory savedCategory = foodCategoryDao.save(category);

        Optional<FoodCategory> foundCategory = foodCategoryDao.findById(savedCategory.getId());
        assertTrue(foundCategory.isPresent(), "FoodCategory должен быть найден.");
        assertEquals(savedCategory.getId(), foundCategory.get().getId(), "ID найденного FoodCategory должно совпадать.");
        assertEquals("Мясо", foundCategory.get().getName(), "Имя найденного FoodCategory должно совпадать.");
    }
}
