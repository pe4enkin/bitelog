package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FoodCategoryDaoTest {
    private FoodCategoryDao foodCategoryDao;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnectionManager.configureForTesting(":memory:");
        foodCategoryDao = new FoodCategoryDao();
        foodCategoryDao.createTables();
    }

    @Test
    @DisplayName("Метод save должен сохранить FoodCategory")
    void save_shouldSaveFoodCategory() throws SQLException {
        FoodCategory meat = new FoodCategory(1, "Мясо");
        FoodCategory savedMeat = foodCategoryDao.save(meat);

        assertNotNull(savedMeat.getId(), "ID сохраненного FoodCategory не должен быть null.");
        assertEquals("Мясо", savedMeat.getName(), "Имя FoodCategory должно совпадать.");

        Optional<FoodCategory> foundMeat = foodCategoryDao.findById(savedMeat.getId());
        assertTrue(foundMeat.isPresent(), "Сохраненный FoodCategory должен быть найден по ID.");
        assertEquals("Мясо", foundMeat.get().getName(), "Имя сохраненного FoodCategory должно совпадать.");
    }
}
