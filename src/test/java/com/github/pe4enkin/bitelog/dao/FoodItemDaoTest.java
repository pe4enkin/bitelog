package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import com.github.pe4enkin.bitelog.model.FoodComponent;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.model.Unit;
import com.github.pe4enkin.bitelog.sql.SqlQueries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FoodItemDaoTest {
    private FoodItemDao foodItemDao;
    private FoodCategoryDao foodCategoryDao;
    private FoodCategory category;
    private DataSource testDataSource;
    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnectionManager.configureForTesting("file:memdb1?mode=memory&cache=shared&foreign_keys=on");
        testDataSource = DatabaseConnectionManager.getDataSource();
        testConnection = testDataSource.getConnection();
        foodItemDao = new FoodItemDao(testDataSource);
        foodCategoryDao = new FoodCategoryDao(testDataSource);
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(SqlQueries.DROP_TABLE_FOOD_ITEMS);
            stmt.execute(SqlQueries.DROP_TABLE_FOOD_COMPONENTS);
            stmt.execute(SqlQueries.DROP_TABLE_FOOD_CATEGORIES);
        }
        foodItemDao.createTables();
        foodCategoryDao.createTables();
        category = foodCategoryDao.save(new FoodCategory("Еда"));
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
    @DisplayName("Метод save должен сохранить простой FoodItem и сгенерировать ID.")
    void save_shouldSaveNonCompositeFoodItem() throws SQLException {
        FoodItem item = new FoodItem.Builder()
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem savedItem = foodItemDao.save(item);

        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0, "ID сохраненного FoodItem должен быть сгенерирован и быть больше 0.");
        assertEquals("Говядина", savedItem.getName(), "Имя сохраненного FoodItem должно совпадать.");
        assertEquals(250.0, savedItem.getCaloriesPer100g(), 0.001, "Калорийность сохраненного FoodItem должна совпадать.");
        assertEquals(Unit.GRAM, savedItem.getUnit(), "Мера измерения сохраненного FoodItem должна совпадать.");
        assertEquals(19.0, savedItem.getProteinsPer100g(), 0.001, "Белки сохраненного FoodItem должны совпадать.");
        assertEquals(16.0, savedItem.getFatsPer100g(), 0.001, "Жиры сохраненного FoodItem должны совпадать.");
        assertEquals(1.0, savedItem.getCarbsPer100g(), 0.001, "Углеводы сохраненного FoodItem должны совпадать.");
        assertFalse(savedItem.isComposite(), "FoodItem должен остаться простым.");
        assertEquals(category, savedItem.getFoodCategory(), "Категория сохраненного FoodItem должна совпадать.");
        assertNull(savedItem.getComponents());
    }

    @Test
    @DisplayName("Метод save должен сохранить составной FoodItem и сгенерировать ID.")
    void save_shouldSaveCompositeFoodItem() throws SQLException {
        FoodItem flour = new FoodItem.Builder()
                .setName("Мука")
                .setCaloriesPer100g(350.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.CUP)
                .setProteinsPer100g(10.0)
                .setFatsPer100g(1.0)
                .setCarbsPer100g(70.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem sugar = new FoodItem.Builder()
                .setName("Сахар")
                .setCaloriesPer100g(300.0)
                .setServingSizeInGrams(10.0)
                .setUnit(Unit.TABLESPOON)
                .setProteinsPer100g(0.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(100.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem savedFlour = foodItemDao.save(flour);
        FoodItem savedSugar = foodItemDao.save(sugar);
        FoodComponent component1 = new FoodComponent(savedFlour.getId(), 150.0);
        FoodComponent component2 = new FoodComponent(savedSugar.getId(), 50.0);
        List<FoodComponent> components = Arrays.asList(component1, component2);

        FoodItem item = new FoodItem.Builder()
                .setName("Тесто")
                .setCaloriesPer100g(337.5)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(7.5)
                .setFatsPer100g(0.75)
                .setCarbsPer100g(77.5)
                .setComposite(true)
                .setFoodCategory(category)
                .setComponents(components)
                .build();

        FoodItem savedItem = foodItemDao.save(item);

        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0, "ID сохраненного FoodItem должен быть сгенерирован и быть больше 0.");
        assertEquals("Тесто", savedItem.getName(), "Имя сохраненного FoodItem должно совпадать.");
        assertEquals(337.5, savedItem.getCaloriesPer100g(), 0.001, "Калорийность сохраненного FoodItem должна совпадать.");
        assertEquals(Unit.GRAM, savedItem.getUnit(), "Мера измерения сохраненного FoodItem должна совпадать.");
        assertEquals(7.5, savedItem.getProteinsPer100g(), 0.001, "Белки сохраненного FoodItem должны совпадать.");
        assertEquals(0.75, savedItem.getFatsPer100g(), 0.001, "Жиры сохраненного FoodItem должны совпадать.");
        assertEquals(77.5, savedItem.getCarbsPer100g(), 0.001, "Углеводы сохраненного FoodItem должны совпадать.");
        assertTrue(savedItem.isComposite(), "FoodItem должен остаться составным.");
        assertEquals(category, savedItem.getFoodCategory(), "Категория сохраненного FoodItem должна совпадать.");
        assertNotNull(savedItem.getComponents(), "Список компонентов сохраненного FoodItem должен сохраниться.");
        assertEquals(2, savedItem.getComponents().size(), "Список компонентов сохраненного FoodItem должен содержать 2 компонента.");

        Map<Long, Double> expectedComponentAmounts = new HashMap<>();
        expectedComponentAmounts.put(savedFlour.getId(), 150.0);
        expectedComponentAmounts.put(savedSugar.getId(), 50.0);
        Map<Long, Double> actualComponentAmounts = new HashMap<>();
        for (FoodComponent component : savedItem.getComponents()) {
            assertTrue(component.getId() > 0, "ID сохраненного компонента при сохранении FoodItem должен быть сгенерирован и быть больше 0.");
            actualComponentAmounts.put(component.getIngredientFoodItemId(), component.getAmountInGrams());
        }
        assertEquals(expectedComponentAmounts, actualComponentAmounts, "Ингредиенты и их количество не должны измениться при сохранении FoodItem.");
    }

    @Test
    @DisplayName("SQLException при вызове метода save на FoodItem с неуникальным именем.")
    void save_shouldThrowSQLExceptionOnDuplicateName() throws SQLException {
        FoodItem item = new FoodItem.Builder()
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem duplicateItem = new FoodItem.Builder()
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        foodItemDao.save(item);
        assertThrows(SQLException.class, () -> {
            foodItemDao.save(duplicateItem);
        }, "Должно быть SQLException при сохранении FoodItem с неуникальным именем.");
        List<FoodItem> allItems = foodItemDao.findAll(false);
        assertEquals(1, allItems.size(), "В БД должен сохраниться только один food item.");
        assertEquals("Говядина", allItems.get(0).getName(), "Имя food item должно совпадать.");
    }

    @Test
    @DisplayName("SQLException при неудачной вставке.")
    void save_shouldThrowSQLExceptionOnFailedInsert() throws SQLException {
        FoodCategory invalidCategory = new FoodCategory(999L, "Несуществующая категория.");
        FoodItem invalidItem = new FoodItem.Builder()
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(invalidCategory)
                .setComponents(null)
                .build();

        assertThrows(SQLException.class, () -> {
            foodItemDao.save(invalidItem);
        }, "Должно быть SQLException при сохранении FoodItem с несуществующей категорией.");
        assertFalse(foodItemDao.findByName("Говядина").isPresent(), "food item не должен сохраниться после отката транзакции.");
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