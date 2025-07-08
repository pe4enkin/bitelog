package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.model.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

class FoodItemDaoTest {
    private FoodItemDao foodItemDao;
    private DataSource testDataSource;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnectionManager.configureForTesting("file:memdb1?mode=memory&cache=shared");
        foodItemDao = new FoodItemDao(testDataSource);
        foodItemDao.createTables();
    }

    @Test
    @DisplayName("Сохранение простого food item.")
    void shouldSaveSimpleFoodItem() throws SQLException {
        FoodCategory foodCategory = new FoodCategory(1, "Мясо");
        FoodItem apple = new FoodItem.Builder()
                .setId(1)
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(foodCategory)
                .setComponents(null)
                .build();
    }
}