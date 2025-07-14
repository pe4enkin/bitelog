package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.dao.exception.DuplicateKeyException;
import com.github.pe4enkin.bitelog.dao.exception.ForeignKeyViolationException;
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

            assertTrue(actualComponentMap.containsKey(ingredientId), "Компонент у найденного FoodIten должен содержать ингредиент с ID " + ingredientId);
            FoodComponent actualComponent = actualComponentMap.get(ingredientId);
            assertEquals(expectedComponent.getId(), actualComponent.getId(), "Список компонентов найденного FoodItem должен сохранить ID компонентов.");
            assertEquals(expectedComponent.getIngredientFoodItemId(), actualComponent.getIngredientFoodItemId(), "Список компонентов найденного FoodItem должен сохранить ID ингредиентов.");
            assertEquals(expectedComponent.getAmountInGrams(), actualComponent.getAmountInGrams(), "Список компонентов найденного FoodItem должен сохранить вес ингредиентов. ");
        }
    }

    @Test
    @DisplayName("Метод findById должен вернуть Optional.empty, если food item не существует.")
    void findById_shouldReturnEmptyForNonExistentFoodCategory() {
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

            assertTrue(actualComponentMap.containsKey(ingredientId), "Компонент у найденного FoodIten должен содержать ингредиент с ID " + ingredientId);
            FoodComponent actualComponent = actualComponentMap.get(ingredientId);
            assertEquals(expectedComponent.getId(), actualComponent.getId(), "Список компонентов найденного FoodItem должен сохранить ID компонентов.");
            assertEquals(expectedComponent.getIngredientFoodItemId(), actualComponent.getIngredientFoodItemId(), "Список компонентов найденного FoodItem должен сохранить ID ингредиентов.");
            assertEquals(expectedComponent.getAmountInGrams(), actualComponent.getAmountInGrams(), "Список компонентов найденного FoodItem должен сохранить вес ингредиентов. ");
        }
    }

    @Test
    @DisplayName("Метод findByName должен вернуть Optional.empty, если food item не существует.")
    void findByName_shouldReturnEmptyForNonExistentFoodCategory() {
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
        List<FoodComponent> components = Arrays.asList(component1, component2);

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
        List<FoodComponent> updatedComponents = Arrays.asList(updatedComponent1, updatedComponent2);

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
    void update_shouldReturnFalseForNonExistentFoodCategory() {
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
    @DisplayName("Метод delete должен успешно удалить food category.")
    void delete_shouldDeleteFoodCategory() {
        FoodCategory category = new FoodCategory("Мясо");
        FoodCategory savedCategory = foodCategoryDao.save(category);

        boolean deleted = foodCategoryDao.delete(savedCategory.getId());
        assertTrue(deleted, "food category должен быть удален.");

        Optional<FoodCategory> foundDeleted = foodCategoryDao.findById(savedCategory.getId());
        assertFalse(foundDeleted.isPresent(), "Удаленный food category не должен быть найден.");
    }

    @Test
    @DisplayName("Метод delete должен вернуть false если food category не существует.")
    void delete_shouldReturnFalseForNonExistentFoodCategory() {
        boolean deleted = foodCategoryDao.delete(999L);
        assertFalse(deleted, "Delete должен вернуть false для несуществующей food category");
    }

    @Test
    @DisplayName("Метод findAll должен возвращать все существующие food category.")
    void findAll_shouldReturnAllFoodCategories() {
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
    void findAll_shouldReturnEmptyListIfNoCategories() {
        List<FoodCategory> allCategories = foodCategoryDao.findAll();
        assertNotNull(allCategories);
        assertTrue(allCategories.isEmpty(), "Метод findAll должен вернуть пустой список если food category нет.");
    }
}