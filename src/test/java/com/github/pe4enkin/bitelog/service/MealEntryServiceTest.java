package com.github.pe4enkin.bitelog.service;

import com.github.pe4enkin.bitelog.dao.MealEntryDao;
import com.github.pe4enkin.bitelog.dao.exception.DataAccessException;
import com.github.pe4enkin.bitelog.model.*;
import com.github.pe4enkin.bitelog.service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MealEntryServiceTest {

    @Mock
    private MealEntryDao mealEntryDao;

    @Mock
    private FoodItemService foodItemService;

    @InjectMocks
    private MealEntryService mealEntryService;

    private FoodItem foodItem1;
    private FoodItem foodItem2;
    private MealComponent mealComponent1;
    private MealComponent mealComponent2;
    private MealEntry mealEntry;
    private MealEntry savedMealEntry;

    @BeforeEach
    void setUp() {
        foodItem1 = new FoodItem.Builder()
                .setId(100L)
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(null)
                .build();

        foodItem2 = new FoodItem.Builder()
                .setId(101L)
                .setName("Курица")
                .setCaloriesPer100g(190.0)
                .setServingSizeInGrams(350.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(16.0)
                .setFatsPer100g(14.0)
                .setCarbsPer100g(0.5)
                .setComposite(false)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(null)
                .build();

        mealComponent1 = new MealComponent(1L, 100L, 50);
        mealComponent2 = new MealComponent(2L, 101L, 300);
        mealEntry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 07, 29))
                .setTime(LocalTime.of(19, 30))
                .setMealCategory(MealCategory.DINNER)
                .setTotalCalories(0.0)
                .setTotalProteins(0.0)
                .setTotalFats(0.0)
                .setTotalCarbs(0.0)
                .setNotes("notes")
                .setComponents(List.of(mealComponent1, mealComponent2))
                .build();
        savedMealEntry = new MealEntry.Builder()
                .setId(100L)
                .setDate(LocalDate.of(2025, 07, 29))
                .setTime(LocalTime.of(19, 30))
                .setMealCategory(MealCategory.DINNER)
                .setTotalCalories(0.0)
                .setTotalProteins(0.0)
                .setTotalFats(0.0)
                .setTotalCarbs(0.0)
                .setNotes("notes")
                .setComponents(List.of(mealComponent1, mealComponent2))
                .build();
    }

    @Test
    @DisplayName("Метод createMealEntry должен успешно создать MealEntry.")
    void createMealEntry_shouldCreateMealEntry() {
        when(mealEntryDao.save(mealEntry)).thenReturn(savedMealEntry);
        when(foodItemService.getFoodItemById(100L)).thenReturn(Optional.of(foodItem1));
        when(foodItemService.getFoodItemById(101L)).thenReturn(Optional.of(foodItem2));

        MealEntry createdMealEntry = mealEntryService.createMealEntry(mealEntry);

        assertNotNull(createdMealEntry);
        assertEquals(100L, createdMealEntry.getId(), "ID должен быть присвоен после операции создания.");
        assertEquals(LocalDate.of(2025, 07, 29), createdMealEntry.getDate(), "После операции создания мы должны получить обратно запись с той же датой.");
        assertEquals(695.0, createdMealEntry.getTotalCalories(), 0.001, "Значение калорийности должно корректно рассчитаться после операции создания.");
        assertEquals(57.5, createdMealEntry.getTotalProteins(), 0.001, "Значение белков должно корректно рассчитаться после операции создания.");
        assertEquals(50.0, createdMealEntry.getTotalFats(), 0.001, "Значение жиров должно корректно рассчитаться после операции создания.");
        assertEquals(2.0, createdMealEntry.getTotalCarbs(), 0.001, "Значение углеводов должно корректно рассчитаться после операции создания.");
        verify(mealEntryDao, times(1)).save(mealEntry);
        verify(foodItemService, times(1)).getFoodItemById(100L);
        verify(foodItemService, times(1)).getFoodItemById(101L);
    }

    @Test
    @DisplayName("Метод createMealEntry должен успешно создать MealEntry с пустым списком.")
    void createMealEntry_shouldCreateMealEntryWithEmptyList() {
        mealEntry.setComponents(new ArrayList<>());
        savedMealEntry.setComponents(new ArrayList<>());
        when(mealEntryDao.save(mealEntry)).thenReturn(savedMealEntry);

        MealEntry createdMealEntry = mealEntryService.createMealEntry(mealEntry);

        assertNotNull(createdMealEntry);
        assertEquals(100L, createdMealEntry.getId(), "ID должен быть присвоен после операции создания.");
        assertEquals(LocalDate.of(2025, 07, 29), createdMealEntry.getDate(), "После операции создания мы должны получить обратно запись с той же датой.");
        assertEquals(0.0, createdMealEntry.getTotalCalories(), 0.001, "Значение калорийности должно быть 0 при пустом списке.");
        assertEquals(0.0, createdMealEntry.getTotalProteins(), 0.001, "Значение белков должно быть 0 при пустом списке.");
        assertEquals(0.0, createdMealEntry.getTotalFats(), 0.001, "Значение жиров должно быть 0 при пустом списке.");
        assertEquals(0.0, createdMealEntry.getTotalCarbs(), 0.001, "Значение углеводов должно быть 0 при пустом списке.");
        verify(mealEntryDao, times(1)).save(mealEntry);
        verify(foodItemService, never()).getFoodItemById(anyLong());
    }

    @Test
    @DisplayName("ServiceException при вызове метода createMealEntry на MealEntry с несуществующим продуктом")
    void createMealEntry_shouldThrowServiceExceptionOnNonExistentFoodItem() {
        when(mealEntryDao.save(mealEntry)).thenReturn(savedMealEntry);
        when(foodItemService.getFoodItemById(anyLong())).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> mealEntryService.createMealEntry(mealEntry),
                "Должно быть ServiceException при вызове метода createMealEntry на MealEntry с несуществующим продуктом.");

        assertEquals("Не удалось рассчитать нутриенты: продукт с ID 100 не найден.", exception.getMessage(),
                "Сообщение об ошибке должно указывать на проблему с ненайденным продуктом.");
        verify(foodItemService, times(1)).getFoodItemById(anyLong());
    }

    @Test
    @DisplayName("ServiceException при ошибке DAO во время вызова метода createMealEntry.")
    void createMealEntry_shouldThrowServiceExceptionOnDataAccessException() {
        when(mealEntryDao.save(mealEntry)).thenThrow(new DataAccessException("Ошибка БД."));

        ServiceException exception = assertThrows(ServiceException.class, () -> mealEntryService.createMealEntry(mealEntry),
                "Должно быть ServiceException при ошибке DAO во время вызова метода createMealEntry.");
        assertTrue(exception.getMessage().contains("Не удалось создать MealEntry от 29.07.2025 в 19:30"),
                "Сообщение об ошибке должно указывать на проблему с созданием MealEntry.");
        verify(mealEntryDao).save(mealEntry);
    }

    @Test
    @DisplayName("Метод getMealEntryById должен вернуть существующий MealEntry и расчитать нутриенты.")
    void getMealEntryById_shouldReturnExistingMealEntryAndCalculateNutrients() {
        when(foodItemService.getFoodItemById(100L)).thenReturn(Optional.of(foodItem1));
        when(foodItemService.getFoodItemById(101L)).thenReturn(Optional.of(foodItem2));

        when(mealEntryDao.findById(100L)).thenReturn(Optional.of(savedMealEntry));

        Optional<MealEntry> foundMealEntry = mealEntryService.getMealEntryById(100L);

        assertTrue(foundMealEntry.isPresent(), "MealEntry должен быть найден.");
        assertEquals(100L, foundMealEntry.get().getId(), "ID найденного MealEntry должно совпадать.");
        assertEquals(LocalDate.of(2025, 07, 29), foundMealEntry.get().getDate(), "Дата найденного MealEntry должна совпадать.");
        assertEquals(695.0, foundMealEntry.get().getTotalCalories(), 0.001, "Значение калорийности должно корректно рассчитаться после операции поиска.");
        assertEquals(57.5, foundMealEntry.get().getTotalProteins(), 0.001, "Значение белков должно корректно рассчитаться после операции поиска.");
        assertEquals(50.0, foundMealEntry.get().getTotalFats(), 0.001, "Значение жиров должно корректно рассчитаться после операции поиска.");
        assertEquals(2.0, foundMealEntry.get().getTotalCarbs(), 0.001, "Значение углеводов должно корректно рассчитаться после операции поиска.");

        verify(mealEntryDao, times(1)).findById(100L);
        verify(foodItemService, times(1)).getFoodItemById(100L);
        verify(foodItemService, times(1)).getFoodItemById(101L);
    }

    @Test
    @DisplayName("Метод getMealEntryById должен вернуть Optional.empty, если meal entry не существует.")
    void getMealEntryById_shouldReturnEmptyForNonExistentMealEntry() {
        when(mealEntryDao.findById(999L)).thenReturn(Optional.empty());

        Optional<MealEntry> foundMealEntry = mealEntryService.getMealEntryById(999L);

        assertFalse(foundMealEntry.isPresent(), "MealEntry не должен быть найден.");

        verify(mealEntryDao, times(1)).findById(999L);
        verify(foodItemService, never()).getFoodItemById(anyLong());
    }

    @Test
    @DisplayName("ServiceException при ошибке DAO во время вызова метода getMealEntryById.")
    void getMealEntryById_shouldThrowServiceExceptionOnDataAccessException() {
        when(mealEntryDao.findById(100L)).thenThrow(new DataAccessException("Ошибка БД."));

        ServiceException exception = assertThrows(ServiceException.class, () -> mealEntryService.getMealEntryById(100L),
                "Должно быть ServiceException при ошибке DAO во время вызова метода getMealEntryById.");
        assertTrue(exception.getMessage().contains("Не удалось получить MealEntry по ID 100"),
                "Сообщение об ошибке должно указывать на проблему с поиском MealEntry.");
        verify(mealEntryDao).findById(100L);
    }

    @Test
    @DisplayName("Метод updateMealEntry должен успешно обновить MealEntry.")
    void updateMealEntry_shouldUpdateMealEntry() {
        when(mealEntryDao.update(savedMealEntry)).thenReturn(true);
        when(foodItemService.getFoodItemById(100L)).thenReturn(Optional.of(foodItem1));
        when(foodItemService.getFoodItemById(101L)).thenReturn(Optional.of(foodItem2));

        MealEntry updatedMealEntry = mealEntryService.updateMealEntry(savedMealEntry);

        assertNotNull(updatedMealEntry);
        assertEquals(100L, updatedMealEntry.getId(), "ID должен сохраниться после операции обновления.");
        assertEquals(LocalDate.of(2025, 07, 29), updatedMealEntry.getDate(), "После операции обновления мы должны получить обратно MealEntry с той же датой.");
        assertEquals(695.0, updatedMealEntry.getTotalCalories(), 0.001, "Значение калорийности должно корректно рассчитаться после операции обновления.");
        assertEquals(57.5, updatedMealEntry.getTotalProteins(), 0.001, "Значение белков должно корректно рассчитаться после операции обновления.");
        assertEquals(50.0, updatedMealEntry.getTotalFats(), 0.001, "Значение жиров должно корректно рассчитаться после операции обновления.");
        assertEquals(2.0, updatedMealEntry.getTotalCarbs(), 0.001, "Значение углеводов должно корректно рассчитаться после операции обновления.");
        verify(mealEntryDao, times(1)).update(savedMealEntry);
        verify(foodItemService, times(1)).getFoodItemById(100L);
        verify(foodItemService, times(1)).getFoodItemById(101L);
    }

    @Test
    @DisplayName("ServiceException при вызове метода updateMealEntry на MealEntry с ID <= 0.")
    void updateMealEntry_shouldThrowServiceExceptionOnInvalidId() {
        ServiceException exception = assertThrows(ServiceException.class, () -> mealEntryService.updateMealEntry(mealEntry),
                "Должно быть ServiceException при вызове метода updateMealEntry на MealEntry с ID <= 0.");
        assertEquals("ID MealEntry должен быть указан для обновления.", exception.getMessage(),
                "Сообщение об ошибке должно указывать на проблему с ID.");
        verify(mealEntryDao, never()).update(any(MealEntry.class));
    }

    @Test
    @DisplayName("ServiceException при вызове метода updateMealEntry на MealEntry с несуществующим продуктом")
    void updateMealEntry_shouldThrowServiceExceptionOnNonExistentFoodItem() {
        when(mealEntryDao.update(savedMealEntry)).thenReturn(true);
        when(foodItemService.getFoodItemById(anyLong())).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> mealEntryService.updateMealEntry(savedMealEntry),
                "Должно быть ServiceException при вызове метода updateMealEntry на MealEntry с несуществующим продуктом.");

        assertEquals("Не удалось рассчитать нутриенты: продукт с ID 100 не найден.", exception.getMessage(),
                "Сообщение об ошибке должно указывать на проблему с ненайденным продуктом.");
        verify(foodItemService, times(1)).getFoodItemById(anyLong());
    }

    @Test
    @DisplayName("ServiceException при ошибке DAO во время вызова метода updateMealEntry.")
    void updateMealEntry_shouldThrowServiceExceptionOnDataAccessException() {
        when(mealEntryDao.update(savedMealEntry)).thenThrow(new DataAccessException("Ошибка БД."));

        ServiceException exception = assertThrows(ServiceException.class, () -> mealEntryService.updateMealEntry(savedMealEntry),
                "Должно быть ServiceException при ошибке DAO во время вызова метода updateMealEntry.");
        assertTrue(exception.getMessage().contains("Не удалось обновить MealEntry от 29.07.2025 в 19:30"),
                "Сообщение об ошибке должно указывать на проблему с обновлением MealEntry.");
        verify(mealEntryDao).update(savedMealEntry);
    }

    @Test
    @DisplayName("Метод deleteMealEntry должен вернуть true в случае успешного удаления meal entry.")
    void deleteMealEntry_shouldReturnTrue() {
        when(mealEntryDao.delete(100L)).thenReturn(true);

        boolean deleted = mealEntryService.deleteMealEntry(100L);

        assertTrue(deleted, "Должен вернуть true в случае успешного удаления meal entry на уровне DAO.");
        verify(mealEntryDao).delete(100L);
    }

    @Test
    @DisplayName("Метод deleteMealEntry должен вернуть false в случае неуспешного удаления meal entry.")
    void deleteMealEntry_shouldReturnFalse() {
        when(mealEntryDao.delete(100L)).thenReturn(false);

        boolean deleted = mealEntryService.deleteMealEntry(100L);

        assertFalse(deleted, "Должен вернуть false в случае неуспешного удаления meal entry на уровне DAO.");
        verify(mealEntryDao).delete(100L);
    }

    @Test
    @DisplayName("ServiceException при ошибке DAO во время вызова метода deleteMealEntry.")
    void deleteMealEntry_shouldThrowServiceExceptionOnDataAccessException() {
        when(mealEntryDao.delete(100L)).thenThrow(new DataAccessException("Ошибка БД."));

        ServiceException exception = assertThrows(ServiceException.class, () -> mealEntryService.deleteMealEntry(100L),
                "Должно быть ServiceException при ошибке DAO во время вызова метода updateMealEntry.");
        assertTrue(exception.getMessage().contains("Не удалось удалить MealEntry с ID 100:"),
                "Сообщение об ошибке должно указывать на проблему с удалением MealEntry.");
        verify(mealEntryDao).delete(100L);
    }

    @Test
    @DisplayName("Метод getAllByDate должен возвращать список MealEntry на нужную дату с загрузкой компонентов и расчетом нутриентов.")
    void getAllByDate_shouldReturnAllMealEntriesByDateAndCalculateNutrients() {
        when(foodItemService.getFoodItemById(100L)).thenReturn(Optional.of(foodItem1));
        when(foodItemService.getFoodItemById(101L)).thenReturn(Optional.of(foodItem2));

        MealEntry anotherSavedMealEntry = new MealEntry.Builder()
                .setId(101L)
                .setDate(LocalDate.of(2025, 07, 29))
                .setTime(LocalTime.of(19, 30))
                .setMealCategory(MealCategory.DINNER)
                .setTotalCalories(0.0)
                .setTotalProteins(0.0)
                .setTotalFats(0.0)
                .setTotalCarbs(0.0)
                .setNotes("notes2")
                .setComponents(List.of(new MealComponent(3L, foodItem1.getId(), 200)))
                .build();

        when(mealEntryDao.findAllByDate(LocalDate.of(2025, 07, 29))).thenReturn(List.of(savedMealEntry, anotherSavedMealEntry));
        List<MealEntry> mealEntries = mealEntryService.getAllByDate(LocalDate.of(2025, 07, 29));

        assertNotNull(mealEntries);
        assertEquals(2, mealEntries.size(), "В списке должно быть 2 записи.");
        assertEquals(100L, mealEntries.get(0).getId(), "ID найденного MealEntry должно совпадать.");
        assertEquals(LocalDate.of(2025, 07, 29), mealEntries.get(0).getDate(), "Дата найденного MealEntry должна совпадать.");
        assertEquals(695.0, mealEntries.get(0).getTotalCalories(), 0.001, "Значение калорийности должно корректно рассчитаться после операции поиска.");
        assertEquals(57.5, mealEntries.get(0).getTotalProteins(), 0.001, "Значение белков должно корректно рассчитаться после операции поиска.");
        assertEquals(50.0, mealEntries.get(0).getTotalFats(), 0.001, "Значение жиров должно корректно рассчитаться после операции поиска.");
        assertEquals(2.0, mealEntries.get(0).getTotalCarbs(), 0.001, "Значение углеводов должно корректно рассчитаться после операции поиска.");
        assertEquals(101L, mealEntries.get(1).getId(), "ID найденного MealEntry должно совпадать.");
        assertEquals(LocalDate.of(2025, 07, 29), mealEntries.get(1).getDate(), "Дата найденного MealEntry должна совпадать.");
        assertEquals(500.0, mealEntries.get(1).getTotalCalories(), 0.001, "Значение калорийности должно корректно рассчитаться после операции поиска.");
        assertEquals(38.0, mealEntries.get(1).getTotalProteins(), 0.001, "Значение белков должно корректно рассчитаться после операции поиска.");
        assertEquals(32.0, mealEntries.get(1).getTotalFats(), 0.001, "Значение жиров должно корректно рассчитаться после операции поиска.");
        assertEquals(2.0, mealEntries.get(1).getTotalCarbs(), 0.001, "Значение углеводов должно корректно рассчитаться после операции поиска.");

        verify(mealEntryDao).findAllByDate(LocalDate.of(2025, 07, 29));
        verify(foodItemService, times(2)).getFoodItemById(100L);
        verify(foodItemService, times(1)).getFoodItemById(101L);
    }

    @Test
    @DisplayName("Метод getAllByDate должен возвращать пустой список если записей на нужную дату нет.")
    void getAllByDate_shouldReturnAEmptyListIfNoEntriesByDate() {
        when(mealEntryDao.findAllByDate(LocalDate.of(2025, 07, 29))).thenReturn(new ArrayList<>());
        List<MealEntry> mealEntries = mealEntryService.getAllByDate(LocalDate.of(2025, 07, 29));

        assertNotNull(mealEntries);
        assertTrue(mealEntries.isEmpty(), "Метод getAllByDate должен вернуть пустой список если meal entry нет.");

        verify(mealEntryDao).findAllByDate(LocalDate.of(2025, 07, 29));
        verify(foodItemService, never()).getFoodItemById(anyLong());
    }

    @Test
    @DisplayName("ServiceException при ошибке DAO во время вызова метода getAllByDate.")
    void getAllByDate_shouldThrowServiceExceptionOnDataAccessException() {
        when(mealEntryDao.findAllByDate(LocalDate.of(2025, 07, 29))).thenThrow(new DataAccessException("Ошибка БД."));

        ServiceException exception = assertThrows(ServiceException.class, () -> mealEntryService.getAllByDate(LocalDate.of(2025, 07, 29)),
                "Должно быть ServiceException при ошибке DAO во время вызова метода getAllByDate.");
        assertTrue(exception.getMessage().contains("Не удалось получить список MealEntries на дату 29.07.2025:"),
                "Сообщение об ошибке должно указывать на проблему с поиском MealEntry.");
        verify(mealEntryDao).findAllByDate(LocalDate.of(2025, 07, 29));
    }
}