package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.dao.exception.DuplicateKeyException;
import com.github.pe4enkin.bitelog.dao.exception.ForeignKeyViolationException;
import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.*;
import com.github.pe4enkin.bitelog.sql.SqlQueries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FoodItemDaoTest {
    private FoodItemDao foodItemDao;
    private FoodCategoryDao foodCategoryDao;
    private FoodCategory category;
    private DataSource testDataSource;
    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnectionManager.configureForTesting("file:memdb1?mode=memory&cache=shared");
        testDataSource = DatabaseConnectionManager.getDataSource();
        testConnection = testDataSource.getConnection();
        foodItemDao = new FoodItemDao(testDataSource);
        foodCategoryDao = new FoodCategoryDao(testDataSource);
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(SqlQueries.DROP_TABLE_FOOD_COMPONENTS);
            stmt.execute(SqlQueries.DROP_TABLE_FOOD_ITEMS);
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

    private int countFoodComponentsByParentId(long parentFoodItemId) throws SQLException {
        try (PreparedStatement pstmt = testConnection.prepareStatement(SqlQueries.SELECT_COUNT_FOOD_COMPONENTS)) {
            pstmt.setLong(1, parentFoodItemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    @Test
    @DisplayName("Метод save должен сохранить простой FoodItem и сгенерировать ID.")
    void save_shouldSaveNonCompositeFoodItem() {
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
        assertEquals(200.0, savedItem.getServingSizeInGrams(), 0.001, "Вес порции сохраненного FoodItem должен совпадать.");
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
    void save_shouldSaveCompositeFoodItem() {
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
        List<FoodComponent> components = List.of(component1, component2);

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
        assertEquals(200.0, savedItem.getServingSizeInGrams(), 0.001, "Вес порции сохраненного FoodItem должен совпадать.");
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
    @DisplayName("DuplicateKeyException при вызове метода save на FoodItem с неуникальным именем.")
    void save_shouldThrowDuplicateKeyExceptionOnDuplicateName() {
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
        assertThrows(DuplicateKeyException.class, () -> {
            foodItemDao.save(duplicateItem);
        }, "Должно быть DuplicateKeyException при сохранении FoodItem с неуникальным именем.");
        List<FoodItem> allItems = foodItemDao.findAll(false);
        assertEquals(1, allItems.size(), "В БД должен сохраниться только один food item.");
        assertEquals("Говядина", allItems.get(0).getName(), "Имя food item должно совпадать.");
    }

    @Test
    @DisplayName("ForeignKeyViolationException при нарушении внешнего ключа.")
    void save_shouldThrowForeignKeyViolationExceptionOnForeignKeyViolation() {
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

        assertThrows(ForeignKeyViolationException.class, () -> {
            foodItemDao.save(invalidItem);
        }, "Должно быть ForeignKeyViolationException при сохранении FoodItem с несуществующей категорией.");
        assertFalse(foodItemDao.findByName("Говядина").isPresent(), "food item не должен сохраниться после отката транзакции.");
    }

    @Test
    @DisplayName("Метод findById должен найти простой существующий food item по ID.")
    void findById_shouldReturnExistingNonCompositeFoodItem() {
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
        Optional<FoodItem> foundItem = foodItemDao.findById(savedItem.getId());

        assertTrue(foundItem.isPresent(), "food item должен быть найден.");
        assertEquals(savedItem.getId(), foundItem.get().getId(), "ID найденного food item должно совпадать.");
        assertEquals("Говядина", foundItem.get().getName(), "Имя найденного food item должно совпадать.");
        assertEquals(250.0, foundItem.get().getCaloriesPer100g(), 0.001, "Калорийность найденного FoodItem должна совпадать.");
        assertEquals(200.0, foundItem.get().getServingSizeInGrams(), 0.001, "Вес порции найденного FoodItem должен совпадать.");
        assertEquals(Unit.GRAM, foundItem.get().getUnit(), "Мера измерения найденного FoodItem должна совпадать.");
        assertEquals(19.0, foundItem.get().getProteinsPer100g(), 0.001, "Белки найденного FoodItem должны совпадать.");
        assertEquals(16.0, foundItem.get().getFatsPer100g(), 0.001, "Жиры найденного FoodItem должны совпадать.");
        assertEquals(1.0, foundItem.get().getCarbsPer100g(), 0.001, "Углеводы найденного FoodItem должны совпадать.");
        assertFalse(foundItem.get().isComposite(), "Найденный FoodItem должен также быть простым.");
        assertEquals(category, foundItem.get().getFoodCategory(), "Категория найденного FoodItem должна совпадать.");
        assertNull(savedItem.getComponents());
    }

    @Test
    @DisplayName("Метод findById должен найти составной существующий food item по ID.")
    void findById_shouldReturnExistingCompositeFoodItem() {
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
        List<FoodComponent> components = List.of(component1, component2);

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
        Optional<FoodItem> foundItem = foodItemDao.findById(savedItem.getId());

        assertTrue(foundItem.isPresent(), "food item должен быть найден.");
        assertEquals(savedItem.getId(), foundItem.get().getId(), "ID найденного food item должно совпадать.");
        assertEquals("Тесто", foundItem.get().getName(), "Имя найденного food item должно совпадать.");
        assertEquals(337.5, foundItem.get().getCaloriesPer100g(), 0.001, "Калорийность найденного FoodItem должна совпадать.");
        assertEquals(200.0, foundItem.get().getServingSizeInGrams(), 0.001, "Вес порции найденного FoodItem должен совпадать.");
        assertEquals(Unit.GRAM, foundItem.get().getUnit(), "Мера измерения найденного FoodItem должна совпадать.");
        assertEquals(7.5, foundItem.get().getProteinsPer100g(), 0.001, "Белки найденного FoodItem должны совпадать.");
        assertEquals(0.75, foundItem.get().getFatsPer100g(), 0.001, "Жиры найденного FoodItem должны совпадать.");
        assertEquals(77.5, foundItem.get().getCarbsPer100g(), 0.001, "Углеводы найденного FoodItem должны совпадать.");
        assertTrue(foundItem.get().isComposite(), "Найденный FoodItem должен также быть составным.");
        assertEquals(category, foundItem.get().getFoodCategory(), "Категория найденного FoodItem должна совпадать.");
        assertNotNull(foundItem.get().getComponents(), "Список компонентов найденного FoodItem должен сохраниться.");

        Map<Long, FoodComponent> expectedComponentMap = savedItem.getComponents().stream()
                        .collect(Collectors.toMap(FoodComponent::getIngredientFoodItemId, Function.identity()));
        Map<Long, FoodComponent> actualComponentMap = foundItem.get().getComponents().stream()
                        .collect(Collectors.toMap(FoodComponent::getIngredientFoodItemId, Function.identity()));

        assertEquals(expectedComponentMap.size(), actualComponentMap.size(), "Количество компонентов найденного FoodItem должно совпадать.");

        for (Map.Entry<Long, FoodComponent> entry : expectedComponentMap.entrySet()) {
            Long ingredientId = entry.getKey();
            FoodComponent expectedComponent = entry.getValue();

            assertTrue(actualComponentMap.containsKey(ingredientId), "Компонент у найденного FoodItem должен содержать ингредиент с ID " + ingredientId);
            FoodComponent actualComponent = actualComponentMap.get(ingredientId);
            assertEquals(expectedComponent.getId(), actualComponent.getId(), "Список компонентов найденного FoodItem должен сохранить ID компонентов.");
            assertEquals(expectedComponent.getIngredientFoodItemId(), actualComponent.getIngredientFoodItemId(), "Список компонентов найденного FoodItem должен сохранить ID ингредиентов.");
            assertEquals(expectedComponent.getAmountInGrams(), actualComponent.getAmountInGrams(), "Список компонентов найденного FoodItem должен сохранить вес ингредиентов. ");
        }
    }

    @Test
    @DisplayName("Метод findById должен вернуть Optional.empty, если food item не существует.")
    void findById_shouldReturnEmptyForNonExistentFoodItem() {
        Optional<FoodItem> foundItem = foodItemDao.findById(999L);
        assertFalse(foundItem.isPresent(), "food item не должен быть найден.");
    }

    @Test
    @DisplayName("Метод findByName должен найти простой существующий food item по имени.")
    void findByName_shouldReturnExistingNonCompositeFoodItem() {
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
        Optional<FoodItem> foundItem = foodItemDao.findByName(savedItem.getName());

        assertTrue(foundItem.isPresent(), "food item должен быть найден.");
        assertEquals(savedItem.getId(), foundItem.get().getId(), "ID найденного food item должно совпадать.");
        assertEquals("Говядина", foundItem.get().getName(), "Имя найденного food item должно совпадать.");
        assertEquals(250.0, foundItem.get().getCaloriesPer100g(), 0.001, "Калорийность найденного FoodItem должна совпадать.");
        assertEquals(200.0, foundItem.get().getServingSizeInGrams(), 0.001, "Вес порции найденного FoodItem должен совпадать.");
        assertEquals(Unit.GRAM, foundItem.get().getUnit(), "Мера измерения найденного FoodItem должна совпадать.");
        assertEquals(19.0, foundItem.get().getProteinsPer100g(), 0.001, "Белки найденного FoodItem должны совпадать.");
        assertEquals(16.0, foundItem.get().getFatsPer100g(), 0.001, "Жиры найденного FoodItem должны совпадать.");
        assertEquals(1.0, foundItem.get().getCarbsPer100g(), 0.001, "Углеводы найденного FoodItem должны совпадать.");
        assertFalse(foundItem.get().isComposite(), "Найденный FoodItem должен также быть простым.");
        assertEquals(category, foundItem.get().getFoodCategory(), "Категория найденного FoodItem должна совпадать.");
        assertNull(savedItem.getComponents());
    }

    @Test
    @DisplayName("Метод findByName должен найти составной существующий food item по имени.")
    void findByName_shouldReturnExistingCompositeFoodItem() {
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
        List<FoodComponent> components = List.of(component1, component2);

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
        Optional<FoodItem> foundItem = foodItemDao.findByName(savedItem.getName());

        assertTrue(foundItem.isPresent(), "food item должен быть найден.");
        assertEquals(savedItem.getId(), foundItem.get().getId(), "ID найденного food item должно совпадать.");
        assertEquals("Тесто", foundItem.get().getName(), "Имя найденного food item должно совпадать.");
        assertEquals(337.5, foundItem.get().getCaloriesPer100g(), 0.001, "Калорийность найденного FoodItem должна совпадать.");
        assertEquals(200.0, foundItem.get().getServingSizeInGrams(), 0.001, "Вес порции найденного FoodItem должен совпадать.");
        assertEquals(Unit.GRAM, foundItem.get().getUnit(), "Мера измерения найденного FoodItem должна совпадать.");
        assertEquals(7.5, foundItem.get().getProteinsPer100g(), 0.001, "Белки найденного FoodItem должны совпадать.");
        assertEquals(0.75, foundItem.get().getFatsPer100g(), 0.001, "Жиры найденного FoodItem должны совпадать.");
        assertEquals(77.5, foundItem.get().getCarbsPer100g(), 0.001, "Углеводы найденного FoodItem должны совпадать.");
        assertTrue(foundItem.get().isComposite(), "Найденный FoodItem должен также быть составным.");
        assertEquals(category, foundItem.get().getFoodCategory(), "Категория найденного FoodItem должна совпадать.");
        assertNotNull(foundItem.get().getComponents(), "Список компонентов найденного FoodItem должен сохраниться.");

        Map<Long, FoodComponent> expectedComponentMap = savedItem.getComponents().stream()
                .collect(Collectors.toMap(FoodComponent::getIngredientFoodItemId, Function.identity()));
        Map<Long, FoodComponent> actualComponentMap = foundItem.get().getComponents().stream()
                .collect(Collectors.toMap(FoodComponent::getIngredientFoodItemId, Function.identity()));

        assertEquals(expectedComponentMap.size(), actualComponentMap.size(), "Количество компонентов найденного FoodItem должно совпадать.");

        for (Map.Entry<Long, FoodComponent> entry : expectedComponentMap.entrySet()) {
            Long ingredientId = entry.getKey();
            FoodComponent expectedComponent = entry.getValue();

            assertTrue(actualComponentMap.containsKey(ingredientId), "Компонент у найденного FoodItem должен содержать ингредиент с ID " + ingredientId);
            FoodComponent actualComponent = actualComponentMap.get(ingredientId);
            assertEquals(expectedComponent.getId(), actualComponent.getId(), "Список компонентов найденного FoodItem должен сохранить ID компонентов.");
            assertEquals(expectedComponent.getIngredientFoodItemId(), actualComponent.getIngredientFoodItemId(), "Список компонентов найденного FoodItem должен сохранить ID ингредиентов.");
            assertEquals(expectedComponent.getAmountInGrams(), actualComponent.getAmountInGrams(), "Список компонентов найденного FoodItem должен сохранить вес ингредиентов. ");
        }
    }

    @Test
    @DisplayName("Метод findByName должен вернуть Optional.empty, если food item не существует.")
    void findByName_shouldReturnEmptyForNonExistentFoodItem() {
        Optional<FoodItem> foundItem = foodItemDao.findByName("Несуществующее имя");
        assertFalse(foundItem.isPresent(), "food item не должен быть найден.");
    }

    @Test
    @DisplayName("Метод update должен успешно обновить простой food item до простого food item.")
    void update_shouldUpdateNonCompositeFoodItemToNonComposite() {
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

        FoodCategory updatedCategory = foodCategoryDao.save(new FoodCategory("Обновленная категория"));

        savedItem.setName("Курица");
        savedItem.setCaloriesPer100g(190.0);
        savedItem.setServingSizeInGrams(350.0);
        savedItem.setUnit(Unit.PACK);
        savedItem.setProteinsPer100g(16.0);
        savedItem.setFatsPer100g(14.0);
        savedItem.setCarbsPer100g(0.5);
        savedItem.setFoodCategory(updatedCategory);

        boolean updated = foodItemDao.update(savedItem);
        assertTrue(updated, "food item должен быть обновлен.");

        Optional<FoodItem> foundUpdated = foodItemDao.findById(savedItem.getId());
        assertTrue(foundUpdated.isPresent(), "Обновленный food item должен быть найден.");
        assertEquals("Курица", foundUpdated.get().getName(), "Имя food item должно обновиться.");
        assertEquals(190.0, foundUpdated.get().getCaloriesPer100g(), 0.001, "Калорийность food item должна обновиться.");
        assertEquals(350.0, foundUpdated.get().getServingSizeInGrams(), 0.001, "Вес порции food item должен обновиться.");
        assertEquals(Unit.PACK, foundUpdated.get().getUnit(), "Мера измерения FoodItem должна обновиться.");
        assertEquals(16.0, foundUpdated.get().getProteinsPer100g(), 0.001, "Белки food item должны обновиться.");
        assertEquals(14.0, foundUpdated.get().getFatsPer100g(), 0.001, "Жиры food item должны обновиться.");
        assertEquals(0.5, foundUpdated.get().getCarbsPer100g(), 0.001, "Углеводы food item должны обновиться.");
        assertFalse(foundUpdated.get().isComposite(), "Обновленный food item должен остаться простым.");
        assertEquals(updatedCategory, foundUpdated.get().getFoodCategory(), "Категория food item должна обновиться.");
        assertNull(foundUpdated.get().getComponents());
    }

    @Test
    @DisplayName("Метод update должен успешно обновить простой food item до составного food item.")
    void update_shouldUpdateNonCompositeFoodItemToComposite() {
        FoodItem item = new FoodItem.Builder()
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(150.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem savedItem = foodItemDao.save(item);

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
        List<FoodComponent> components = List.of(component1, component2);

        FoodCategory updatedCategory = foodCategoryDao.save(new FoodCategory("Обновленная категория"));

        savedItem.setName("Тесто");
        savedItem.setCaloriesPer100g(337.5);
        savedItem.setServingSizeInGrams(200.0);
        savedItem.setUnit(Unit.PACK);
        savedItem.setProteinsPer100g(7.5);
        savedItem.setFatsPer100g(0.75);
        savedItem.setCarbsPer100g(77.5);
        savedItem.setComposite(true);
        savedItem.setFoodCategory(updatedCategory);
        savedItem.setComponents(components);

        boolean updated = foodItemDao.update(savedItem);
        assertTrue(updated, "food item должен быть обновлен.");

        Optional<FoodItem> foundUpdated = foodItemDao.findById(savedItem.getId());
        assertTrue(foundUpdated.isPresent(), "Обновленный food item должен быть найден.");
        assertEquals("Тесто", foundUpdated.get().getName(), "Имя food item должно обновиться.");
        assertEquals(337.5, foundUpdated.get().getCaloriesPer100g(), 0.001, "Калорийность food item должна обновиться.");
        assertEquals(200.0, foundUpdated.get().getServingSizeInGrams(), 0.001, "Вес порции food item должен обновиться.");
        assertEquals(Unit.PACK, foundUpdated.get().getUnit(), "Мера измерения FoodItem должна обновиться.");
        assertEquals(7.5, foundUpdated.get().getProteinsPer100g(), 0.001, "Белки food item должны обновиться.");
        assertEquals(0.75, foundUpdated.get().getFatsPer100g(), 0.001, "Жиры food item должны обновиться.");
        assertEquals(77.5, foundUpdated.get().getCarbsPer100g(), 0.001, "Углеводы food item должны обновиться.");
        assertTrue(foundUpdated.get().isComposite(), "Обновленный food item должен стать составным.");
        assertEquals(updatedCategory, foundUpdated.get().getFoodCategory(), "Категория food item должна обновиться.");
        assertNotNull(foundUpdated.get().getComponents(), "У обновленного food item должен появиться список компонентов.");

        Map<Long, Double> expectedComponentAmounts = new HashMap<>();
        expectedComponentAmounts.put(savedFlour.getId(), 150.0);
        expectedComponentAmounts.put(savedSugar.getId(), 50.0);
        Map<Long, Double> actualComponentAmounts = new HashMap<>();
        for (FoodComponent component : foundUpdated.get().getComponents()) {
            assertTrue(component.getId() > 0, "ID сохраненного компонента при обновлении FoodItem должен быть сгенерирован и быть больше 0.");
            actualComponentAmounts.put(component.getIngredientFoodItemId(), component.getAmountInGrams());
        }
        assertEquals(expectedComponentAmounts, actualComponentAmounts, "Ингредиенты и их количество должны корректно сохраниться при обновлении FoodItem.");
    }

    @Test
    @DisplayName("Метод update должен успешно обновить составной food item до простого food item.")
    void update_shouldUpdateCompositeFoodItemToNonComposite() {
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
        List<FoodComponent> components = List.of(component1, component2);

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

        FoodCategory updatedCategory = foodCategoryDao.save(new FoodCategory("Обновленная категория"));

        savedItem.setName("Курица");
        savedItem.setCaloriesPer100g(190.0);
        savedItem.setServingSizeInGrams(350.0);
        savedItem.setUnit(Unit.PACK);
        savedItem.setProteinsPer100g(16.0);
        savedItem.setFatsPer100g(14.0);
        savedItem.setCarbsPer100g(0.5);
        savedItem.setComposite(false);
        savedItem.setFoodCategory(updatedCategory);
        savedItem.setComponents(null);

        boolean updated = foodItemDao.update(savedItem);
        assertTrue(updated, "food item должен быть обновлен.");

        Optional<FoodItem> foundUpdated = foodItemDao.findById(savedItem.getId());
        assertTrue(foundUpdated.isPresent(), "Обновленный food item должен быть найден.");
        assertEquals("Курица", foundUpdated.get().getName(), "Имя food item должно обновиться.");
        assertEquals(190.0, foundUpdated.get().getCaloriesPer100g(), 0.001, "Калорийность food item должна обновиться.");
        assertEquals(350.0, foundUpdated.get().getServingSizeInGrams(), 0.001, "Вес порции food item должен обновиться.");
        assertEquals(Unit.PACK, foundUpdated.get().getUnit(), "Мера измерения FoodItem должна обновиться.");
        assertEquals(16.0, foundUpdated.get().getProteinsPer100g(), 0.001, "Белки food item должны обновиться.");
        assertEquals(14.0, foundUpdated.get().getFatsPer100g(), 0.001, "Жиры food item должны обновиться.");
        assertEquals(0.5, foundUpdated.get().getCarbsPer100g(), 0.001, "Углеводы food item должны обновиться.");
        assertFalse(foundUpdated.get().isComposite(), "Обновленный food item должен стать простым.");
        assertEquals(updatedCategory, foundUpdated.get().getFoodCategory(), "Категория food item должна обновиться.");
        assertNull(foundUpdated.get().getComponents());
    }

    @Test
    @DisplayName("Метод update должен успешно обновить составной food item до составного food item.")
    void update_shouldUpdateCompositeFoodItemToComposite() {
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
        List<FoodComponent> components = List.of(component1, component2);

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

        FoodItem chicken = new FoodItem.Builder()
                .setName("Курица")
                .setCaloriesPer100g(190.0)
                .setServingSizeInGrams(350.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(16.0)
                .setFatsPer100g(14.0)
                .setCarbsPer100g(0.5)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem vegetables = new FoodItem.Builder()
                .setName("Овощи")
                .setCaloriesPer100g(65.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(3.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(9.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem savedChicken = foodItemDao.save(chicken);
        FoodItem savedVegetables = foodItemDao.save(vegetables);
        FoodComponent updatedComponent1 = new FoodComponent(savedChicken.getId(), 150.0);
        FoodComponent updatedComponent2 = new FoodComponent(savedVegetables.getId(), 100.0);
        List<FoodComponent> updatedComponents = List.of(updatedComponent1, updatedComponent2);

        FoodCategory updatedCategory = foodCategoryDao.save(new FoodCategory("Обновленная категория"));

        savedItem.setName("Курица с овощами");
        savedItem.setCaloriesPer100g(140.0);
        savedItem.setServingSizeInGrams(250.0);
        savedItem.setUnit(Unit.PACK);
        savedItem.setProteinsPer100g(10.8);
        savedItem.setFatsPer100g(8.4);
        savedItem.setCarbsPer100g(3.9);
        savedItem.setFoodCategory(updatedCategory);
        savedItem.setComponents(updatedComponents);

        boolean updated = foodItemDao.update(savedItem);
        assertTrue(updated, "food item должен быть обновлен.");

        Optional<FoodItem> foundUpdated = foodItemDao.findById(savedItem.getId());
        assertTrue(foundUpdated.isPresent(), "Обновленный food item должен быть найден.");
        assertEquals("Курица с овощами", foundUpdated.get().getName(), "Имя food item должно обновиться.");
        assertEquals(140.0, foundUpdated.get().getCaloriesPer100g(), 0.001, "Калорийность food item должна обновиться.");
        assertEquals(250.0, foundUpdated.get().getServingSizeInGrams(), 0.001, "Вес порции food item должен обновиться.");
        assertEquals(Unit.PACK, foundUpdated.get().getUnit(), "Мера измерения FoodItem должна обновиться.");
        assertEquals(10.8, foundUpdated.get().getProteinsPer100g(), 0.001, "Белки food item должны обновиться.");
        assertEquals(8.4, foundUpdated.get().getFatsPer100g(), 0.001, "Жиры food item должны обновиться.");
        assertEquals(3.9, foundUpdated.get().getCarbsPer100g(), 0.001, "Углеводы food item должны обновиться.");
        assertTrue(foundUpdated.get().isComposite(), "Обновленный food item должен остаться составным.");
        assertEquals(updatedCategory, foundUpdated.get().getFoodCategory(), "Категория food item должна обновиться.");
        assertNotNull(foundUpdated.get().getComponents(), "У обновленного food item должен быть список компонентов.");

        Map<Long, Double> expectedComponentAmounts = new HashMap<>();
        expectedComponentAmounts.put(savedChicken.getId(), 150.0);
        expectedComponentAmounts.put(savedVegetables.getId(), 100.0);
        Map<Long, Double> actualComponentAmounts = new HashMap<>();
        for (FoodComponent component : foundUpdated.get().getComponents()) {
            assertTrue(component.getId() > 0, "ID сохраненного компонента при обновлении FoodItem должен быть сгенерирован и быть больше 0.");
            actualComponentAmounts.put(component.getIngredientFoodItemId(), component.getAmountInGrams());
        }
        assertEquals(expectedComponentAmounts, actualComponentAmounts, "Ингредиенты и их количество должны корректно сохраниться при обновлении FoodItem.");
    }

    @Test
    @DisplayName("Метод update должен удалить компоненты из БД перед вставкой новых.")
    void update_shouldDeleteComponentsFromDatabase() throws SQLException {
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
        List<FoodComponent> components = List.of(component1, component2);

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

        int componentCount = countFoodComponentsByParentId(savedItem.getId());
        assertEquals(2, componentCount, "После сохранения в базе должно быть 2 компонента.");

        savedItem.setComponents(null);
        boolean updated = foodItemDao.update(savedItem);

        assertTrue(updated, "Food Item должен успешно обновиться.");
        componentCount = countFoodComponentsByParentId(savedItem.getId());
        assertEquals(0, componentCount, "После обновления в базе не должно быть компонентов.");
    }

    @Test
    @DisplayName("DuplicateKeyException при вызове метода update на FoodItem с неуникальным именем.")
    void update_shouldThrowDuplicateKeyExceptionOnDuplicateName() {
        FoodItem item1 = new FoodItem.Builder()
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(150.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem item2 = new FoodItem.Builder()
                .setName("Курица")
                .setCaloriesPer100g(190.0)
                .setServingSizeInGrams(350.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(16.0)
                .setFatsPer100g(14.0)
                .setCarbsPer100g(0.5)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem savedItem1 = foodItemDao.save(item1);
        FoodItem savedItem2 = foodItemDao.save(item2);

        savedItem2.setName("Говядина");

        assertThrows(DuplicateKeyException.class, () -> {
            foodItemDao.update(savedItem2);
        }, "Должно быть DuplicateKeyException при обновлении FoodItem с неуникальным именем.");

        Optional<FoodItem> foundAfterUpdate = foodItemDao.findById(savedItem2.getId());
        assertTrue(foundAfterUpdate.isPresent(), "food item должен остаться после неудачной попытки обновления.");
        assertEquals("Курица", foundAfterUpdate.get().getName(), "Имя food item не должно измениться после попытки обновления.");
    }

    @Test
    @DisplayName("Метод update должен вернуть false если food item не существует.")
    void update_shouldReturnFalseForNonExistentFoodItem() {
        FoodItem nonExistentItem = new FoodItem.Builder()
                .setName("Несуществующее имя")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(150.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();
        boolean updated = foodItemDao.update(nonExistentItem);
        assertFalse(updated, "Update должен вернуть false для несуществующего food item");
    }

    @Test
    @DisplayName("Метод delete должен успешно удалить простой food item не являющийся ингредиентом.")
    void delete_shouldDeleteNonCompositeNonComponentFoodItem() {
        FoodItem item = new FoodItem.Builder()
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(150.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();
        FoodItem savedItem = foodItemDao.save(item);

        boolean deleted = foodItemDao.delete(savedItem.getId());
        assertTrue(deleted, "food item должен быть удален.");

        Optional<FoodItem> foundDeleted = foodItemDao.findById(savedItem.getId());
        assertFalse(foundDeleted.isPresent(), "Удаленный food item не должен быть найден.");
    }

    @Test
    @DisplayName("Метод delete должен успешно удалить составной food item с компонентами.")
    void delete_shouldDeleteCompositeFoodItemAndComponents() throws SQLException {
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
        List<FoodComponent> components = List.of(component1, component2);

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

        boolean deleted = foodItemDao.delete(savedItem.getId());
        assertTrue(deleted, "food item должен быть удален.");

        Optional<FoodItem> foundDeleted = foodItemDao.findById(savedItem.getId());
        assertFalse(foundDeleted.isPresent(), "Удаленный food item не должен быть найден.");

        int componentCount = countFoodComponentsByParentId(savedItem.getId());
        assertEquals(0, componentCount, "После удаления в базе не должно быть компонентов.");
    }

    @Test
    @DisplayName("ForeignKeyViolationException при вызове метода delete на продукте являющимся ингредиентом")
    void delete_shouldThrowForeignKeyViolationExceptionOnDeleteComponentFoodItem() {
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

        FoodItem savedFlour = foodItemDao.save(flour);
        FoodComponent component = new FoodComponent(savedFlour.getId(), 200.0);
        List<FoodComponent> components = List.of(component);

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

        assertThrows(ForeignKeyViolationException.class, () -> {
            foodItemDao.delete(savedFlour.getId());
        }, "Должно быть ForeignKeyViolationException при удалении FoodItem являющегося ингредиентом.");
        assertTrue(foodItemDao.findById(savedFlour.getId()).isPresent(), "food item должен остаться в базе после неудачной попытки удаления.");
    }

    @Test
    @DisplayName("ForeignKeyViolationException при вызове метода delete на продукте являющимся компонентом для meal entry")
    void delete_shouldThrowForeignKeyViolationExceptionOnDeleteMealComponentFoodItem() {
        FoodItem item = new FoodItem.Builder()
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(150.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();
        FoodItem savedItem = foodItemDao.save(item);
        MealEntryDao mealEntryDao = new MealEntryDao(testDataSource);
        mealEntryDao.createTables();
        MealComponent mealComponent = new MealComponent(savedItem.getId(), 100);
        List<MealComponent> components = List.of(mealComponent);
        MealEntry mealEntry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(components)
                .build();
        mealEntryDao.save(mealEntry);

        assertThrows(ForeignKeyViolationException.class, () -> {
            foodItemDao.delete(savedItem.getId());
        }, "Должно быть ForeignKeyViolationException при удалении FoodItem являющегося компонентом для meal entry.");
        assertTrue(foodItemDao.findById(savedItem.getId()).isPresent(), "food item должен остаться в базе после неудачной попытки удаления.");
    }

    @Test
    @DisplayName("Метод delete должен вернуть false если food item не существует.")
    void delete_shouldReturnFalseForNonExistentFoodItem() {
        boolean deleted = foodItemDao.delete(999L);
        assertFalse(deleted, "Delete должен вернуть false для несуществующего food item");
    }

    @Test
    @DisplayName("Метод findAll(false) должен возвращать все существующие food item без списка компонентов.")
    void findAll_withFalseKeyShouldReturnAllFoodItemsWithoutComponents() {
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
        List<FoodComponent> components1 = List.of(component1, component2);

        FoodItem item1 = new FoodItem.Builder()
                .setName("Тесто")
                .setCaloriesPer100g(337.5)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(7.5)
                .setFatsPer100g(0.75)
                .setCarbsPer100g(77.5)
                .setComposite(true)
                .setFoodCategory(category)
                .setComponents(components1)
                .build();

        FoodItem savedItem1 = foodItemDao.save(item1);

        FoodItem chicken = new FoodItem.Builder()
                .setName("Курица")
                .setCaloriesPer100g(190.0)
                .setServingSizeInGrams(350.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(16.0)
                .setFatsPer100g(14.0)
                .setCarbsPer100g(0.5)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem vegetables = new FoodItem.Builder()
                .setName("Овощи")
                .setCaloriesPer100g(65.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(3.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(9.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem savedChicken = foodItemDao.save(chicken);
        FoodItem savedVegetables = foodItemDao.save(vegetables);
        FoodComponent component3 = new FoodComponent(savedChicken.getId(), 150.0);
        FoodComponent component4 = new FoodComponent(savedVegetables.getId(), 100.0);
        List<FoodComponent> components2 = List.of(component3, component4);

        FoodItem item2 = new FoodItem.Builder()
                .setName("Курица с овощами")
                .setCaloriesPer100g(140.0)
                .setServingSizeInGrams(250.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(10.8)
                .setFatsPer100g(8.4)
                .setCarbsPer100g(3.9)
                .setComposite(true)
                .setFoodCategory(category)
                .setComponents(components2)
                .build();

        FoodItem savedItem2 = foodItemDao.save(item2);

        List<FoodItem> actualFoodItems = foodItemDao.findAll(false);
        assertNotNull(actualFoodItems);
        assertEquals(6, actualFoodItems.size(), "Метод findAll должен найти 6 food item.");

        List<FoodItem> expectedFoodItems = List.of(savedFlour, savedSugar, savedItem1, savedChicken, savedVegetables, savedItem2);
        Map<Long, FoodItem> expectedMap = expectedFoodItems.stream()
                .collect(Collectors.toMap(FoodItem::getId, Function.identity()));
        Map<Long, FoodItem> actualMap = actualFoodItems.stream()
                .collect(Collectors.toMap(FoodItem::getId, Function.identity()));

        for (Map.Entry<Long, FoodItem> entry : expectedMap.entrySet()) {
            Long itemId = entry.getKey();
            FoodItem expectedItem = entry.getValue();

            assertTrue(actualMap.containsKey(itemId), "Найденный список должен содержать FoodItem c ID " + itemId);
            FoodItem actualItem = actualMap.get(itemId);

            assertEquals(expectedItem.getId(), actualItem.getId(), "ID найденных food item должны совпадать.");
            assertEquals(expectedItem.getName(), actualItem.getName(), "Имена найденных food item должны совпадать.");
            assertEquals(expectedItem.getCaloriesPer100g(), actualItem.getCaloriesPer100g(), 0.001,"Калорийность найденных food item должны совпадать.");
            assertEquals(expectedItem.getServingSizeInGrams(), actualItem.getServingSizeInGrams(), 0.001,"Веса порций найденных food item должны совпадать.");
            assertEquals(expectedItem.getUnit(), actualItem.getUnit(), "Меры измерений найденных food item должны совпадать.");
            assertEquals(expectedItem.getProteinsPer100g(), actualItem.getProteinsPer100g(), 0.001,"Белки найденных food item должны совпадать.");
            assertEquals(expectedItem.getFatsPer100g(), actualItem.getFatsPer100g(), 0.001,"Жиры найденных food item должны совпадать.");
            assertEquals(expectedItem.getCarbsPer100g(), actualItem.getCarbsPer100g(), 0.001,"Углеводы найденных food item должны совпадать.");
            assertEquals(expectedItem.isComposite(), actualItem.isComposite(), "Статусы составных найденных food item должны совпадать.");
            assertNull(actualItem.getComponents(), "Списки компонентов найденных food item не должны грузиться.");
        }
    }

    @Test
    @DisplayName("Метод findAll(true) должен возвращать все существующие food item со списком компонентов.")
    void findAll_withTrueKeyShouldReturnAllFoodItemsWithComponents() {
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
        List<FoodComponent> components1 = List.of(component1, component2);

        FoodItem item1 = new FoodItem.Builder()
                .setName("Тесто")
                .setCaloriesPer100g(337.5)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(7.5)
                .setFatsPer100g(0.75)
                .setCarbsPer100g(77.5)
                .setComposite(true)
                .setFoodCategory(category)
                .setComponents(components1)
                .build();

        FoodItem savedItem1 = foodItemDao.save(item1);

        FoodItem chicken = new FoodItem.Builder()
                .setName("Курица")
                .setCaloriesPer100g(190.0)
                .setServingSizeInGrams(350.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(16.0)
                .setFatsPer100g(14.0)
                .setCarbsPer100g(0.5)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem vegetables = new FoodItem.Builder()
                .setName("Овощи")
                .setCaloriesPer100g(65.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(3.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(9.0)
                .setComposite(false)
                .setFoodCategory(category)
                .setComponents(null)
                .build();

        FoodItem savedChicken = foodItemDao.save(chicken);
        FoodItem savedVegetables = foodItemDao.save(vegetables);
        FoodComponent component3 = new FoodComponent(savedChicken.getId(), 150.0);
        FoodComponent component4 = new FoodComponent(savedVegetables.getId(), 100.0);
        List<FoodComponent> components2 = List.of(component3, component4);

        FoodItem item2 = new FoodItem.Builder()
                .setName("Курица с овощами")
                .setCaloriesPer100g(140.0)
                .setServingSizeInGrams(250.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(10.8)
                .setFatsPer100g(8.4)
                .setCarbsPer100g(3.9)
                .setComposite(true)
                .setFoodCategory(category)
                .setComponents(components2)
                .build();

        FoodItem savedItem2 = foodItemDao.save(item2);

        List<FoodItem> actualFoodItems = foodItemDao.findAll(true);
        assertNotNull(actualFoodItems);
        assertEquals(6, actualFoodItems.size(), "Метод findAll должен найти 6 food item.");

        List<FoodItem> expectedFoodItems = List.of(savedFlour, savedSugar, savedItem1, savedChicken, savedVegetables, savedItem2);
        Map<Long, FoodItem> expectedMap = expectedFoodItems.stream()
                .collect(Collectors.toMap(FoodItem::getId, Function.identity()));
        Map<Long, FoodItem> actualMap = actualFoodItems.stream()
                .collect(Collectors.toMap(FoodItem::getId, Function.identity()));

        for (Map.Entry<Long, FoodItem> entry : expectedMap.entrySet()) {
            Long itemId = entry.getKey();
            FoodItem expectedItem = entry.getValue();

            assertTrue(actualMap.containsKey(itemId), "Найденный список должен содержать FoodItem c ID " + itemId);
            FoodItem actualItem = actualMap.get(itemId);

            assertEquals(expectedItem.getId(), actualItem.getId(), "ID найденных food item должны совпадать.");
            assertEquals(expectedItem.getName(), actualItem.getName(), "Имена найденных food item должны совпадать.");
            assertEquals(expectedItem.getCaloriesPer100g(), actualItem.getCaloriesPer100g(), 0.001,"Калорийность найденных food item должны совпадать.");
            assertEquals(expectedItem.getServingSizeInGrams(), actualItem.getServingSizeInGrams(), 0.001,"Веса порций найденных food item должны совпадать.");
            assertEquals(expectedItem.getUnit(), actualItem.getUnit(), "Меры измерений найденных food item должны совпадать.");
            assertEquals(expectedItem.getProteinsPer100g(), actualItem.getProteinsPer100g(), 0.001,"Белки найденных food item должны совпадать.");
            assertEquals(expectedItem.getFatsPer100g(), actualItem.getFatsPer100g(), 0.001,"Жиры найденных food item должны совпадать.");
            assertEquals(expectedItem.getCarbsPer100g(), actualItem.getCarbsPer100g(), 0.001,"Углеводы найденных food item должны совпадать.");
            assertEquals(expectedItem.isComposite(), actualItem.isComposite(), "Статусы isComposite найденных food item должны совпадать.");
            if (expectedItem.isComposite()) {
                assertNotNull(actualItem.getComponents(), "Список компонентов найденных составных food item не должен быть null.");

                Map<Long, FoodComponent> expectedComponentMap = expectedItem.getComponents().stream()
                        .collect(Collectors.toMap(FoodComponent::getIngredientFoodItemId, Function.identity()));
                Map<Long, FoodComponent> actualComponentMap = actualItem.getComponents().stream()
                        .collect(Collectors.toMap(FoodComponent::getIngredientFoodItemId, Function.identity()));

                assertEquals(expectedComponentMap.size(), actualComponentMap.size(), "Количество компонентов найденного food item должно совпадать.");

                for (Map.Entry<Long, FoodComponent> componentEntry : expectedComponentMap.entrySet()) {
                    Long ingredientId = componentEntry.getKey();
                    FoodComponent expectedComponent = componentEntry.getValue();

                    assertTrue(actualComponentMap.containsKey(ingredientId), "Компонент у одного из найденных food item должен содержать ингредиент с ID " + ingredientId);
                    FoodComponent actualComponent = actualComponentMap.get(ingredientId);
                    assertEquals(expectedComponent.getId(), actualComponent.getId(), "Список компонентов у одного из найденных food item должен сохранить ID компонентов.");
                    assertEquals(expectedComponent.getIngredientFoodItemId(), actualComponent.getIngredientFoodItemId(), "Список компонентов у одного из найденных food item должен сохранить ID ингредиентов.");
                    assertEquals(expectedComponent.getAmountInGrams(), actualComponent.getAmountInGrams(), "Список компонентов у одного из найденных food item должен сохранить вес ингредиентов.");
                }
            } else {
                assertNull(actualItem.getComponents());
            }
        }
    }

    @Test
    @DisplayName("Метод findAll должен возвращать пустой список, если food item нет.")
    void findAll_shouldReturnEmptyListIfNoItems() {
        List<FoodItem> allItems = foodItemDao.findAll(false);
        assertNotNull(allItems);
        assertTrue(allItems.isEmpty(), "Метод findAll должен вернуть пустой список если food item нет.");
    }

    @Test
    @DisplayName("При удалении FoodCategory поле foodCategory у foodItem должно стать null")
    void deleteFoodCategory_shouldSetFoodItemCategoryToNull() {
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
        foodCategoryDao.delete(category.getId());
        Optional<FoodItem> foundItem = foodItemDao.findById(savedItem.getId());
        assertTrue(foundItem.isPresent(), "food item должен остаться в БД.");
        assertNull(foundItem.get().getFoodCategory());
    }
}