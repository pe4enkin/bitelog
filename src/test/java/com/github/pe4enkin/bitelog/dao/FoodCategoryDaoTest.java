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
import java.util.List;
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
        testConnection = testDataSource.getConnection();
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
        }
        ;
    }

    @AfterEach
    void tearDown() throws SQLException {
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

        assertNotNull(savedCategory);
        assertTrue(savedCategory.getId() > 0, "ID сохраненного FoodCategory должен быть сгенерирован и быть больше 0.");
        assertEquals("Мясо", savedCategory.getName(), "Имя FoodCategory должно совпадать.");
    }

    @Test
    @DisplayName("SQLException при вызове метода save на FoodCategory с неуникальным именем.")
    void save_shouldThrowSQLExceptionOnDuplicateName() throws SQLException {
        FoodCategory category1 = new FoodCategory("Мясо");
        FoodCategory category2 = new FoodCategory("Мясо");
        foodCategoryDao.save(category1);
        assertThrows(SQLException.class, () -> {
            foodCategoryDao.save(category2);
        }, "Должно быть SQLException при сохранении FoodCategory с неуникальным именем.");
        List<FoodCategory> allCategories = foodCategoryDao.findAll();
        assertEquals(1, allCategories.size(), "В БД должен сохраниться только один food category.");
        assertEquals("Мясо", allCategories.get(0).getName(), "Имя food category должно совпадать.");
    }

    @Test
    @DisplayName("Метод findById должен найти существующую food category по ID.")
    void findById_shouldReturnExistingFoodCategory() throws SQLException {
        FoodCategory category = new FoodCategory("Мясо");
        FoodCategory savedCategory = foodCategoryDao.save(category);

        Optional<FoodCategory> foundCategory = foodCategoryDao.findById(savedCategory.getId());
        assertTrue(foundCategory.isPresent(), "food category должен быть найден.");
        assertEquals(savedCategory.getId(), foundCategory.get().getId(), "ID найденного food category должно совпадать.");
        assertEquals("Мясо", foundCategory.get().getName(), "Имя найденного food category должно совпадать.");
    }

    @Test
    @DisplayName("Метод findById должен вернуть Optional.empty, если food category не существует.")
    void findById_shouldReturnEmptyForNonExistentFoodCategory() throws SQLException {
        Optional<FoodCategory> foundCategory = foodCategoryDao.findById(999L);
        assertFalse(foundCategory.isPresent(), "food category не должен быть найден.");
    }

    @Test
    @DisplayName("Метод findByName должен найти существующую food category по имени.")
    void findByName_shouldReturnExistingFoodCategory() throws SQLException {
        FoodCategory category = new FoodCategory("Мясо");
        FoodCategory savedCategory = foodCategoryDao.save(category);

        Optional<FoodCategory> foundCategory = foodCategoryDao.findByName(savedCategory.getName());
        assertTrue(foundCategory.isPresent(), "food category должен быть найден.");
        assertEquals(savedCategory.getId(), foundCategory.get().getId(), "ID найденного food category должно совпадать.");
        assertEquals("Мясо", foundCategory.get().getName(), "Имя найденного food category должно совпадать.");
    }

    @Test
    @DisplayName("Метод findByName должен вернуть Optional.empty, если food category не существует.")
    void findByName_shouldReturnEmptyForNonExistentFoodCategory() throws SQLException {
        Optional<FoodCategory> foundCategory = foodCategoryDao.findByName("Несуществующее имя");
        assertFalse(foundCategory.isPresent(), "food category не должен быть найден.");
    }

    @Test
    @DisplayName("Метод update должен успешно обновить food category.")
    void update_shouldUpdateFoodCategory() throws SQLException {
        FoodCategory category = new FoodCategory("Мясо");
        FoodCategory savedCategory = foodCategoryDao.save(category);

        savedCategory.setName("Курица");
        boolean updated = foodCategoryDao.update(savedCategory);
        assertTrue(updated, "food category должен быть обновлен.");

        Optional<FoodCategory> foundUpdated = foodCategoryDao.findById(savedCategory.getId());
        assertTrue(foundUpdated.isPresent(), "Обновленный food category должен быть найден.");
        assertEquals("Курица", foundUpdated.get().getName(), "Имя food category должно обновиться.");
    }

    @Test
    @DisplayName("SQLException при вызове метода update на FoodCategory с неуникальным именем.")
    void update_shouldThrowSQLExceptionOnDuplicateName() throws SQLException {
        FoodCategory category1 = new FoodCategory("Мясо");
        FoodCategory category2 = new FoodCategory("Рыба");
        FoodCategory savedCategory1 = foodCategoryDao.save(category1);
        FoodCategory savedCategory2 = foodCategoryDao.save(category2);

        savedCategory2.setName("Мясо");

        assertThrows(SQLException.class, () -> {
            foodCategoryDao.update(savedCategory2);
        }, "Должно быть SQLException при обновлении FoodCategory с неуникальным именем.");

        Optional<FoodCategory> foundAfterUpdate = foodCategoryDao.findById(savedCategory2.getId());
        assertTrue(foundAfterUpdate.isPresent(), "food category должен остаться после неудачной попытки обновления.");
        assertEquals("Рыба", foundAfterUpdate.get().getName(), "Имя food category не должно измениться после попытки обновления.");
    }

    @Test
    @DisplayName("Метод update должен вернуть false если food category не существует.")
    void update_shouldReturnFalseForNonExistentFoodCategory() throws SQLException {
        FoodCategory nonExistentCategory = new FoodCategory(999L, "Несуществующая категория");
        boolean updated = foodCategoryDao.update(nonExistentCategory);
        assertFalse(updated, "Update должен вернуть false для несуществующей food category");
    }

    @Test
    @DisplayName("Метод delete должен успешно удалить food category.")
    void delete_shouldDeleteFoodCategory() throws SQLException {
        FoodCategory category = new FoodCategory("Мясо");
        FoodCategory savedCategory = foodCategoryDao.save(category);

        boolean deleted = foodCategoryDao.delete(savedCategory.getId());
        assertTrue(deleted, "food category должен быть удален.");

        Optional<FoodCategory> foundDeleted = foodCategoryDao.findById(savedCategory.getId());
        assertFalse(foundDeleted.isPresent(), "Удаленный food category не должен быть найден.");
    }

    @Test
    @DisplayName("Метод delete должен вернуть false если food category не существует.")
    void delete_shouldReturnFalseForNonExistentFoodCategory() throws SQLException {
        boolean deleted = foodCategoryDao.delete(999L);
        assertFalse(deleted, "Delete должен вернуть false для несуществующей food category");
    }

    @Test
    @DisplayName("Метод findAll должен возвращать все существующие food category.")
    void findAll_shouldReturnAllFoodCategories() throws SQLException {
        FoodCategory category1 = new FoodCategory("Мясо");
        FoodCategory category2 = new FoodCategory("Рыба");
        FoodCategory category3 = new FoodCategory("Овощи");

        foodCategoryDao.save(category1);
        foodCategoryDao.save(category2);
        foodCategoryDao.save(category3);

        List<FoodCategory> allCategories = foodCategoryDao.findAll();
        assertNotNull(allCategories);
        assertEquals(3, allCategories.size(), "Метод findAll должен найти 3 food category.");

        assertTrue(allCategories.stream().anyMatch(foodCategory -> "Мясо".equals(foodCategory.getName())), "Метод findAll должен найти food category 'Мясо'.");
        assertTrue(allCategories.stream().anyMatch(foodCategory -> "Рыба".equals(foodCategory.getName())), "Метод findAll должен найти food category 'Рыба'.");
        assertTrue(allCategories.stream().anyMatch(foodCategory -> "Овощи".equals(foodCategory.getName())), "Метод findAll должен найти food category 'Овощи'.");
    }

    @Test
    @DisplayName("Метод findAll должен возвращать пустой список, если food category нет.")
    void findAll_shouldReturnEmptyListIfNoCategories() throws SQLException {
        List<FoodCategory> allCategories = foodCategoryDao.findAll();
        assertNotNull(allCategories);
        assertTrue(allCategories.isEmpty(), "Метод findAll должен вернуть пустой список если food category нет.");
    }
}