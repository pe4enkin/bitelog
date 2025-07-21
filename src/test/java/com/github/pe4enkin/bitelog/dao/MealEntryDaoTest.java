package com.github.pe4enkin.bitelog.dao;

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

public class MealEntryDaoTest {
    private MealEntryDao mealEntryDao;
    private FoodItemDao foodItemDao;
    private FoodCategoryDao foodCategoryDao;
    private FoodCategory foodCategory;
    private FoodItem item1;
    private FoodItem item2;
    private DataSource testDataSource;
    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnectionManager.configureForTesting("file:memdb1?mode=memory&cache=shared");
        testDataSource = DatabaseConnectionManager.getDataSource();
        testConnection = testDataSource.getConnection();
        foodItemDao = new FoodItemDao(testDataSource);
        foodCategoryDao = new FoodCategoryDao(testDataSource);
        mealEntryDao = new MealEntryDao(testDataSource);
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(SqlQueries.DROP_TABLE_MEAL_COMPONENTS);
            stmt.execute(SqlQueries.DROP_TABLE_FOOD_COMPONENTS);
            stmt.execute(SqlQueries.DROP_TABLE_MEAL_ENTRIES);
            stmt.execute(SqlQueries.DROP_TABLE_FOOD_ITEMS);
            stmt.execute(SqlQueries.DROP_TABLE_FOOD_CATEGORIES);
        }
        foodItemDao.createTables();
        foodCategoryDao.createTables();
        mealEntryDao.createTables();
        foodCategory = foodCategoryDao.save(new FoodCategory("Еда"));

        item1 = new FoodItem.Builder()
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
        item1 = foodItemDao.save(item1);

        item2 = new FoodItem.Builder()
                .setName("Курица")
                .setCaloriesPer100g(190.0)
                .setServingSizeInGrams(350.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(16.0)
                .setFatsPer100g(14.0)
                .setCarbsPer100g(0.5)
                .setComposite(false)
                .setFoodCategory(foodCategory)
                .setComponents(null)
                .build();
        item2 = foodItemDao.save(item2);

    }

    @AfterEach
    void tearDown() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
        DatabaseConnectionManager.resetToDefault();
        DatabaseConnectionManager.closeDataSource();
    }

    private int countMealComponentsByMealEntryId(long mealEntryId) throws SQLException {
        try (PreparedStatement pstmt = testConnection.prepareStatement(SqlQueries.SELECT_COUNT_MEAL_COMPONENTS)) {
            pstmt.setLong(1, mealEntryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    @Test
    @DisplayName("Метод save должен сохранить MealEntry без компонентов и сгенерировать ID.")
    void save_shouldSaveMealEntryWithoutComponents() {
        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(null)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);

        assertNotNull(savedEntry);
        assertTrue(savedEntry.getId() > 0, "ID сохраненного MealEntry должен быть сгенерирован и быть больше 0.");
        assertEquals(LocalDate.of(2025, 7, 21), savedEntry.getDate(), "Дата сохраненного MealEntry должна совпадать.");
        assertEquals(LocalTime.of(17, 51), savedEntry.getTime(), "Время сохраненного MealEntry должно совпадать.");
        assertEquals(MealCategory.DINNER, savedEntry.getMealCategory(), "Категория сохраненного MealEntry должна совпадать.");
        assertEquals("заметка", savedEntry.getNotes(), "Заметка сохраненного MealEntry должна совпадать.");
        assertNotNull(savedEntry.getComponents(), "Список компонентов сохраненного MealEntry должен сохраниться.");
        assertTrue(savedEntry.getComponents().isEmpty(), "Список компонентов сохраненного MealEntry должен быть пустым.");
    }

    @Test
    @DisplayName("Метод save должен сохранить MealEntry и сгенерировать ID.")
    void save_shouldSaveMealEntry() {
        MealComponent component1 = new MealComponent(item1.getId(), 150);
        MealComponent component2 = new MealComponent(item2.getId(), 100);
        List<MealComponent> components = List.of(component1, component2);

        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(components)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);

        assertNotNull(savedEntry);
        assertTrue(savedEntry.getId() > 0, "ID сохраненного MealEntry должен быть сгенерирован и быть больше 0.");
        assertEquals(LocalDate.of(2025, 7, 21), savedEntry.getDate(), "Дата сохраненного MealEntry должна совпадать.");
        assertEquals(LocalTime.of(17, 51), savedEntry.getTime(), "Время сохраненного MealEntry должно совпадать.");
        assertEquals(MealCategory.DINNER, savedEntry.getMealCategory(), "Категория сохраненного MealEntry должна совпадать.");
        assertEquals("заметка", savedEntry.getNotes(), "Заметка сохраненного MealEntry должна совпадать.");
        assertNotNull(savedEntry.getComponents(), "Список компонентов сохраненного MealEntry должен сохраниться.");
        assertEquals(2, savedEntry.getComponents().size(), "Список компонентов сохраненного MealEntry должен содержать 2 компонента.");

        Map<Long, Double> expectedComponentAmounts = new HashMap<>();
        expectedComponentAmounts.put(item1.getId(), 150.0);
        expectedComponentAmounts.put(item2.getId(), 100.0);
        Map<Long, Double> actualComponentAmounts = new HashMap<>();
        for (MealComponent component : savedEntry.getComponents()) {
            assertTrue(component.getId() > 0, "ID сохраненного компонента при сохранении MealEntry должен быть сгенерирован и быть больше 0.");
            actualComponentAmounts.put(component.getFoodItemId(), component.getAmountInGrams());
        }
        assertEquals(expectedComponentAmounts, actualComponentAmounts, "Продукты и их количество не должны измениться при сохранении MealEntry.");
    }

    @Test
    @DisplayName("Метод findById должен найти существующий meal entry по ID.")
    void findById_shouldReturnExistingMealEntry() {
        MealComponent component1 = new MealComponent(item1.getId(), 150);
        MealComponent component2 = new MealComponent(item2.getId(), 100);
        List<MealComponent> components = List.of(component1, component2);

        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(components)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);
        Optional<MealEntry> foundEntry = mealEntryDao.findById(savedEntry.getId());

        assertTrue(foundEntry.isPresent(), "meal entry должен быть найден.");
        assertEquals(savedEntry.getId(), foundEntry.get().getId(), "ID найденного meal entry должно совпадать.");
        assertEquals(savedEntry.getDate(), foundEntry.get().getDate(), "Дата найденного MealEntry должна совпадать.");
        assertEquals(savedEntry.getTime(), foundEntry.get().getTime(), "Время найденного MealEntry должно совпадать.");
        assertEquals(savedEntry.getMealCategory(), foundEntry.get().getMealCategory(), "Категория найденного MealEntry должна совпадать.");
        assertEquals(savedEntry.getNotes(), foundEntry.get().getNotes(), "Заметка найденного MealEntry должна совпадать.");
        assertNotNull(foundEntry.get().getComponents(), "Список компонентов найденного MealEntry должен сохраниться.");

        Map<Long, MealComponent> expectedComponentMap = savedEntry.getComponents().stream()
                .collect(Collectors.toMap(MealComponent::getFoodItemId, Function.identity()));
        Map<Long, MealComponent> actualComponentMap = foundEntry.get().getComponents().stream()
                .collect(Collectors.toMap(MealComponent::getFoodItemId, Function.identity()));

        assertEquals(expectedComponentMap.size(), actualComponentMap.size(), "Количество компонентов найденного MealEntry должно совпадать.");

        for (Map.Entry<Long, MealComponent> mapEntry : expectedComponentMap.entrySet()) {
            Long foodItemId = mapEntry.getKey();
            MealComponent expectedComponent = mapEntry.getValue();

            assertTrue(actualComponentMap.containsKey(foodItemId), "Компонент у найденного MealEntry должен содержать продукт с ID " + foodItemId);
            MealComponent actualComponent = actualComponentMap.get(foodItemId);
            assertEquals(expectedComponent.getId(), actualComponent.getId(), "Список компонентов найденного MealEntry должен сохранить ID компонентов.");
            assertEquals(expectedComponent.getFoodItemId(), actualComponent.getFoodItemId(), "Список компонентов найденного MealEntry должен сохранить ID продуктов.");
            assertEquals(expectedComponent.getAmountInGrams(), actualComponent.getAmountInGrams(), "Список компонентов найденного MealEntry должен сохранить вес ингредиентов. ");
        }
    }

    @Test
    @DisplayName("Метод findById должен вернуть Optional.empty, если meal entry не существует.")
    void findById_shouldReturnEmptyForNonExistentMealEntry() {
        Optional<MealEntry> foundEntry = mealEntryDao.findById(999L);
        assertFalse(foundEntry.isPresent(), "meal entry не должен быть найден.");
    }

    @Test
    @DisplayName("Метод update должен успешно обновить meal entry без компонентов до meal entry без компонентов.")
    void update_shouldUpdateMealEntryWithoutComponentsToMealEntryWithoutComponents() {
        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(null)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);

        savedEntry.setDate(LocalDate.of(2024, 6, 20));
        savedEntry.setTime(LocalTime.of(16, 50));
        savedEntry.setMealCategory(MealCategory.SNACK);
        savedEntry.setNotes("новая заметка");

        boolean updated = mealEntryDao.update(savedEntry);
        assertTrue(updated, "meal entry должен быть обновлен.");

        Optional<MealEntry> foundUpdated = mealEntryDao.findById(savedEntry.getId());
        assertTrue(foundUpdated.isPresent(), "Обновленный meal entry должен быть найден.");
        assertEquals(LocalDate.of(2024, 6, 20), foundUpdated.get().getDate(), "Дата MealEntry должна обновиться.");
        assertEquals(LocalTime.of(16, 50), foundUpdated.get().getTime(), "Время MealEntry должно обновиться.");
        assertEquals(MealCategory.SNACK, foundUpdated.get().getMealCategory(), "Категория MealEntry должна обновиться.");
        assertEquals("новая заметка", foundUpdated.get().getNotes(), "Заметка MealEntry должна обновиться.");
        assertNotNull(foundUpdated.get().getComponents(), "Список компонентов обновленного MealEntry должен сохраниться.");
        assertTrue(foundUpdated.get().getComponents().isEmpty(), "Список компонентов обновленного MealEntry должен быть пустым.");
    }

    @Test
    @DisplayName("Метод update должен успешно обновить meal entry без компонентов до meal entry с компонентами.")
    void update_shouldUpdateMealEntryWithoutComponentsToMealEntryWithComponents() {
        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(null)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);

        MealComponent component1 = new MealComponent(item1.getId(), 150);
        MealComponent component2 = new MealComponent(item2.getId(), 100);
        List<MealComponent> components = List.of(component1, component2);

        savedEntry.setDate(LocalDate.of(2024, 6, 20));
        savedEntry.setTime(LocalTime.of(16, 50));
        savedEntry.setMealCategory(MealCategory.SNACK);
        savedEntry.setNotes("новая заметка");
        savedEntry.setComponents(components);

        boolean updated = mealEntryDao.update(savedEntry);
        assertTrue(updated, "meal entry должен быть обновлен.");

        Optional<MealEntry> foundUpdated = mealEntryDao.findById(savedEntry.getId());
        assertTrue(foundUpdated.isPresent(), "Обновленный meal entry должен быть найден.");
        assertEquals(LocalDate.of(2024, 6, 20), foundUpdated.get().getDate(), "Дата MealEntry должна обновиться.");
        assertEquals(LocalTime.of(16, 50), foundUpdated.get().getTime(), "Время MealEntry должно обновиться.");
        assertEquals(MealCategory.SNACK, foundUpdated.get().getMealCategory(), "Категория MealEntry должна обновиться.");
        assertEquals("новая заметка", foundUpdated.get().getNotes(), "Заметка MealEntry должна обновиться.");
        assertNotNull(foundUpdated.get().getComponents(), "Список компонентов обновленного MealEntry должен сохраниться.");

        Map<Long, Double> expectedComponentAmounts = new HashMap<>();
        expectedComponentAmounts.put(item1.getId(), 150.0);
        expectedComponentAmounts.put(item2.getId(), 100.0);
        Map<Long, Double> actualComponentAmounts = new HashMap<>();
        for (MealComponent component : foundUpdated.get().getComponents()) {
            assertTrue(component.getId() > 0, "ID сохраненного компонента при обновлении MealEntry должен быть сгенерирован и быть больше 0.");
            actualComponentAmounts.put(component.getFoodItemId(), component.getAmountInGrams());
        }
        assertEquals(expectedComponentAmounts, actualComponentAmounts, "Продукты и их количество должны корректно сохраниться при обновлении MealEntry.");
    }

    @Test
    @DisplayName("Метод update должен успешно обновить meal entry с компонентами до meal entry без компонентов.")
    void update_shouldUpdateMealEntryWithComponentsToMealEntryWithoutComponents() {
        MealComponent component1 = new MealComponent(item1.getId(), 150);
        MealComponent component2 = new MealComponent(item2.getId(), 100);
        List<MealComponent> components = List.of(component1, component2);

        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(components)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);

        savedEntry.setDate(LocalDate.of(2024, 6, 20));
        savedEntry.setTime(LocalTime.of(16, 50));
        savedEntry.setMealCategory(MealCategory.SNACK);
        savedEntry.setNotes("новая заметка");
        savedEntry.setComponents(Collections.emptyList());

        boolean updated = mealEntryDao.update(savedEntry);
        assertTrue(updated, "meal entry должен быть обновлен.");

        Optional<MealEntry> foundUpdated = mealEntryDao.findById(savedEntry.getId());
        assertTrue(foundUpdated.isPresent(), "Обновленный meal entry должен быть найден.");
        assertEquals(LocalDate.of(2024, 6, 20), foundUpdated.get().getDate(), "Дата MealEntry должна обновиться.");
        assertEquals(LocalTime.of(16, 50), foundUpdated.get().getTime(), "Время MealEntry должно обновиться.");
        assertEquals(MealCategory.SNACK, foundUpdated.get().getMealCategory(), "Категория MealEntry должна обновиться.");
        assertEquals("новая заметка", foundUpdated.get().getNotes(), "Заметка MealEntry должна обновиться.");
        assertNotNull(foundUpdated.get().getComponents(), "Список компонентов обновленного MealEntry должен сохраниться.");
        assertTrue(foundUpdated.get().getComponents().isEmpty(), "Список компонентов обновленного MealEntry должен быть пустым.");
    }

    @Test
    @DisplayName("Метод update должен успешно обновить meal entry с компонентами до meal entry с компонентами.")
    void update_shouldUpdateMealEntryWithComponentsToMealEntryWithComponents() {
        MealComponent component1 = new MealComponent(item1.getId(), 150);
        MealComponent component2 = new MealComponent(item2.getId(), 100);
        List<MealComponent> components = List.of(component1, component2);

        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(components)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);

        FoodItem item3 = new FoodItem.Builder()
                .setName("Овощи")
                .setCaloriesPer100g(65.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(3.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(9.0)
                .setComposite(false)
                .setFoodCategory(foodCategory)
                .setComponents(null)
                .build();
        item3 = foodItemDao.save(item3);

        FoodItem item4 = new FoodItem.Builder()
                .setName("Рыба")
                .setCaloriesPer100g(135.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(6.0)
                .setCarbsPer100g(0.0)
                .setComposite(false)
                .setFoodCategory(foodCategory)
                .setComponents(null)
                .build();
        item4 = foodItemDao.save(item4);

        MealComponent newComponent1 = new MealComponent(item3.getId(), 200);
        MealComponent newComponent2 = new MealComponent(item4.getId(), 150);
        List<MealComponent> newComponents = List.of(newComponent1, newComponent2);

        savedEntry.setDate(LocalDate.of(2024, 6, 20));
        savedEntry.setTime(LocalTime.of(16, 50));
        savedEntry.setMealCategory(MealCategory.SNACK);
        savedEntry.setNotes("новая заметка");
        savedEntry.setComponents(newComponents);

        boolean updated = mealEntryDao.update(savedEntry);
        assertTrue(updated, "meal entry должен быть обновлен.");

        Optional<MealEntry> foundUpdated = mealEntryDao.findById(savedEntry.getId());
        assertTrue(foundUpdated.isPresent(), "Обновленный meal entry должен быть найден.");
        assertEquals(LocalDate.of(2024, 6, 20), foundUpdated.get().getDate(), "Дата MealEntry должна обновиться.");
        assertEquals(LocalTime.of(16, 50), foundUpdated.get().getTime(), "Время MealEntry должно обновиться.");
        assertEquals(MealCategory.SNACK, foundUpdated.get().getMealCategory(), "Категория MealEntry должна обновиться.");
        assertEquals("новая заметка", foundUpdated.get().getNotes(), "Заметка MealEntry должна обновиться.");
        assertNotNull(foundUpdated.get().getComponents(), "Список компонентов обновленного MealEntry должен сохраниться.");

        Map<Long, Double> expectedComponentAmounts = new HashMap<>();
        expectedComponentAmounts.put(item3.getId(), 200.0);
        expectedComponentAmounts.put(item4.getId(), 150.0);
        Map<Long, Double> actualComponentAmounts = new HashMap<>();
        for (MealComponent component : foundUpdated.get().getComponents()) {
            assertTrue(component.getId() > 0, "ID сохраненного компонента при обновлении MealEntry должен быть сгенерирован и быть больше 0.");
            actualComponentAmounts.put(component.getFoodItemId(), component.getAmountInGrams());
        }
        assertEquals(expectedComponentAmounts, actualComponentAmounts, "Продукты и их количество должны корректно сохраниться при обновлении MealEntry.");
    }

    @Test
    @DisplayName("Метод update должен удалить компоненты из БД перед вставкой новых.")
    void update_shouldDeleteComponentsFromDatabase() throws SQLException {
        MealComponent component1 = new MealComponent(item1.getId(), 150);
        MealComponent component2 = new MealComponent(item2.getId(), 100);
        List<MealComponent> components = List.of(component1, component2);

        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(components)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);

        int componentCount = countMealComponentsByMealEntryId(savedEntry.getId());
        assertEquals(2, componentCount, "После сохранения в базе должно быть 2 компонента.");

        savedEntry.setComponents(Collections.emptyList());
        boolean updated = mealEntryDao.update(savedEntry);
        assertTrue(updated, "Meal Entry должен успешно обновиться.");

        componentCount = countMealComponentsByMealEntryId(savedEntry.getId());
        assertEquals(0, componentCount, "После обновления в базе не должно быть компонентов.");
    }

    @Test
    @DisplayName("Метод update должен вернуть false если meal entry не существует.")
    void update_shouldReturnFalseForNonExistentMealEntry() {
        MealEntry nonExistentEntry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(null)
                .build();
        boolean updated = mealEntryDao.update(nonExistentEntry);
        assertFalse(updated, "Update должен вернуть false для несуществующего meal entry");
    }

    @Test
    @DisplayName("Метод delete должен успешно удалить meal entry и его компоненты")
    void delete_shouldDeleteMealEntryAndComponents() throws SQLException {
        MealComponent component1 = new MealComponent(item1.getId(), 150);
        MealComponent component2 = new MealComponent(item2.getId(), 100);
        List<MealComponent> components = List.of(component1, component2);

        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(components)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);

        boolean deleted = mealEntryDao.delete(savedEntry.getId());
        assertTrue(deleted, "meal entry должен быть удален.");

        Optional<MealEntry> foundDeleted = mealEntryDao.findById(savedEntry.getId());
        assertFalse(foundDeleted.isPresent(), "Удаленный meal entry не должен быть найден.");

        int componentCount = countMealComponentsByMealEntryId(savedEntry.getId());
        assertEquals(0, componentCount, "После удаления в базе не должно быть компонентов.");
    }

    @Test
    @DisplayName("Метод delete должен вернуть false если meal entry не существует.")
    void delete_shouldReturnFalseForNonExistentMealEntry() {
        boolean deleted = mealEntryDao.delete(999L);
        assertFalse(deleted, "Delete должен вернуть false для несуществующего meal entry");
    }

    @Test
    @DisplayName("Метод findAllByDate должен возвращать все существующие на искомую дату meal entry со списком компонентов.")
    void findAllByDate_shouldReturnAllMealEntriesByDate() {
        MealComponent component1 = new MealComponent(item1.getId(), 150);
        MealComponent component2 = new MealComponent(item2.getId(), 100);
        List<MealComponent> components = List.of(component1, component2);

        MealEntry entry1 = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(components)
                .build();
        MealEntry savedEntry1 = mealEntryDao.save(entry1);

        FoodItem item3 = new FoodItem.Builder()
                .setName("Овощи")
                .setCaloriesPer100g(65.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(3.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(9.0)
                .setComposite(false)
                .setFoodCategory(foodCategory)
                .setComponents(null)
                .build();
        item3 = foodItemDao.save(item3);

        FoodItem item4 = new FoodItem.Builder()
                .setName("Рыба")
                .setCaloriesPer100g(135.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(6.0)
                .setCarbsPer100g(0.0)
                .setComposite(false)
                .setFoodCategory(foodCategory)
                .setComponents(null)
                .build();

        item4 = foodItemDao.save(item4);

        MealComponent component3 = new MealComponent(item3.getId(), 200);
        MealComponent component4 = new MealComponent(item4.getId(), 150);
        List<MealComponent> components2 = List.of(component3, component4);

        MealEntry entry2 = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(16, 50))
                .setMealCategory(MealCategory.SNACK)
                .setNotes("вторая заметка")
                .setComponents(components2)
                .build();

        MealEntry savedEntry2 = mealEntryDao.save(entry2);

        MealEntry entry3 = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(16, 49))
                .setMealCategory(MealCategory.LUNCH)
                .setNotes("третья заметка")
                .setComponents(null)
                .build();

        MealEntry savedEntry3 = mealEntryDao.save(entry3);


        MealEntry entry4 = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 20))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(null)
                .build();

        MealEntry savedEntry4 = mealEntryDao.save(entry4);

        List<MealEntry> actualMealEntries = mealEntryDao.findAllByDate(LocalDate.of(2025, 7, 21));
        assertNotNull(actualMealEntries);
        assertEquals(3, actualMealEntries.size(), "Метод findAll должен найти 3 подходящих meal entry.");

        List<MealEntry> expectedMealEntries = List.of(savedEntry1, savedEntry2, savedEntry3);
        Map<Long, MealEntry> expectedMap = expectedMealEntries.stream()
                .collect(Collectors.toMap(MealEntry::getId, Function.identity()));
        Map<Long, MealEntry> actualMap = actualMealEntries.stream()
                .collect(Collectors.toMap(MealEntry::getId, Function.identity()));

        for (Map.Entry<Long, MealEntry> mapEntry : expectedMap.entrySet()) {
            Long entryId = mapEntry.getKey();
            MealEntry expectedEntry = mapEntry.getValue();

            assertTrue(actualMap.containsKey(entryId), "Найденный список должен содержать MealEntry c ID " + entryId);
            MealEntry actualEntry = actualMap.get(entryId);

            assertEquals(expectedEntry.getId(), actualEntry.getId(), "ID найденных meal entry должны совпадать.");
            assertEquals(expectedEntry.getDate(), actualEntry.getDate(), "Даты найденных meal entry должны совпадать.");
            assertEquals(expectedEntry.getTime(), actualEntry.getTime(), "Время найденных meal entry должны совпадать.");
            assertEquals(expectedEntry.getMealCategory(), actualEntry.getMealCategory(), "Категории найденных meal entry должны совпадать.");
            assertEquals(expectedEntry.getNotes(), actualEntry.getNotes(), "Заметки найденных meal entry должны совпадать.");
            assertNotNull(actualEntry.getComponents(), "Список компонентов найденных meal entry не должен быть null.");

            if (!actualEntry.getComponents().isEmpty()) {
                Map<Long, MealComponent> expectedComponentMap = expectedEntry.getComponents().stream()
                        .collect(Collectors.toMap(MealComponent::getFoodItemId, Function.identity()));
                Map<Long, MealComponent> actualComponentMap = actualEntry.getComponents().stream()
                        .collect(Collectors.toMap(MealComponent::getFoodItemId, Function.identity()));

                assertEquals(expectedComponentMap.size(), actualComponentMap.size(), "Количество компонентов найденного meal entry должно совпадать.");

                for (Map.Entry<Long, MealComponent> componentEntry : expectedComponentMap.entrySet()) {
                    Long foodItemId = componentEntry.getKey();
                    MealComponent expectedComponent = componentEntry.getValue();

                    assertTrue(actualComponentMap.containsKey(foodItemId), "Компонент у одного из найденных meal entry должен содержать продукт с ID " + foodItemId);
                    MealComponent actualComponent = actualComponentMap.get(foodItemId);
                    assertEquals(expectedComponent.getId(), actualComponent.getId(), "Список компонентов у одного из найденных meal entry должен сохранить ID компонентов.");
                    assertEquals(expectedComponent.getFoodItemId(), actualComponent.getFoodItemId(), "Список компонентов у одного из найденных meal entry должен сохранить ID продуктов.");
                    assertEquals(expectedComponent.getAmountInGrams(), actualComponent.getAmountInGrams(), "Список компонентов у одного из найденных meal entry должен сохранить вес продуктов.");
                }
            }
        }
    }

    @Test
    @DisplayName("Метод findAllByDate должен возвращать пустой список, если meal entry с нужной датой нет.")
    void findAllByDate_shouldReturnEmptyListIfNoEntriesByDate() {
        MealEntry entry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 7, 21))
                .setTime(LocalTime.of(17, 51))
                .setMealCategory(MealCategory.DINNER)
                .setNotes("заметка")
                .setComponents(null)
                .build();

        MealEntry savedEntry = mealEntryDao.save(entry);
        List<MealEntry> allEntries = mealEntryDao.findAllByDate(LocalDate.of(2025, 7, 20));
        assertNotNull(allEntries);
        assertTrue(allEntries.isEmpty(), "Метод findAllByDate должен вернуть пустой список если meal entry нет.");
    }
}