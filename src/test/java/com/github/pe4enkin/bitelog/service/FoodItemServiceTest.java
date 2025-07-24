package com.github.pe4enkin.bitelog.service;

import com.github.pe4enkin.bitelog.dao.FoodItemDao;
import com.github.pe4enkin.bitelog.dao.exception.DataAccessException;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import com.github.pe4enkin.bitelog.model.FoodComponent;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.model.Unit;
import com.github.pe4enkin.bitelog.service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FoodItemServiceTest {

    @Mock
    private FoodItemDao foodItemDao;

    @InjectMocks
    private FoodItemService foodItemService;

    private Set<Long> processedFoodItemsIds;

    @BeforeEach
    void setUp() {
        processedFoodItemsIds = new HashSet<>();
    }

    private Optional<FoodItem> mockFindById(long id) {
        if (id == 1L) {
            FoodItem flour = new FoodItem.Builder()
                    .setName("Мука")
                    .setCaloriesPer100g(350.0)
                    .setServingSizeInGrams(200.0)
                    .setUnit(Unit.CUP)
                    .setProteinsPer100g(10.0)
                    .setFatsPer100g(1.0)
                    .setCarbsPer100g(70.0)
                    .setComposite(false)
                    .setFoodCategory(new FoodCategory(1L, "Еда"))
                    .setComponents(null)
                    .build();
            return Optional.of(flour);
        } else if (id == 2L) {
            FoodItem sugar = new FoodItem.Builder()
                    .setName("Сахар")
                    .setCaloriesPer100g(300.0)
                    .setServingSizeInGrams(10.0)
                    .setUnit(Unit.TABLESPOON)
                    .setProteinsPer100g(0.0)
                    .setFatsPer100g(0.0)
                    .setCarbsPer100g(100.0)
                    .setComposite(false)
                    .setFoodCategory(new FoodCategory(1L, "Еда"))
                    .setComponents(null)
                    .build();
            return Optional.of(sugar);
        } else if (id == 3L) {
            List<FoodComponent> components = List.of(
                    new FoodComponent(1L, 150),
                    new FoodComponent(2L, 50)
            );
            FoodItem dough = new FoodItem.Builder()
                    .setName("Тесто")
                    .setCaloriesPer100g(0.0)
                    .setServingSizeInGrams(200.0)
                    .setUnit(Unit.GRAM)
                    .setProteinsPer100g(0.0)
                    .setFatsPer100g(0.0)
                    .setCarbsPer100g(0.0)
                    .setComposite(true)
                    .setFoodCategory(new FoodCategory(1L, "Еда"))
                    .setComponents(components)
                    .build();
            return Optional.of(dough);
        }
        return Optional.empty();
    }

    @Test
    @DisplayName("Метод createFoodItem должен успешно создать простой FoodItem.")
    void createFoodItem_shouldCreateNonCompositeFoodItem() {
        FoodItem item = new FoodItem.Builder()
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
        FoodItem savedItem = new FoodItem.Builder()
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

        when(foodItemDao.findByName("Говядина")).thenReturn(Optional.empty());
        when(foodItemDao.save(item)).thenReturn(savedItem);

        FoodItem createdItem = foodItemService.createFoodItem(item);

        assertNotNull(createdItem);
        assertEquals(100L, createdItem.getId(), "ID должен быть присвоен после операции создания.");
        assertEquals("Говядина", createdItem.getName(), "После операции создания мы должны получить обратно продукт с тем же именем.");
        assertEquals(250.0, createdItem.getCaloriesPer100g(), 0.001, "Значение калорийности не должно измениться для простого продукта.");
        assertEquals(19.0, createdItem.getProteinsPer100g(), 0.001, "Значение белков не должно измениться для простого продукта.");
        assertEquals(16.0, createdItem.getFatsPer100g(), 0.001, "Значение жиров не должно измениться для простого продукта.");
        assertEquals(1.0, createdItem.getCarbsPer100g(), 0.001, "Значение углеводов не должно измениться для простого продукта.");
        verify(foodItemDao).findByName("Говядина");
        verify(foodItemDao).save(item);
        verify(foodItemDao, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Метод createFoodItem должен успешно создать составной FoodItem и рассчитать нутриенты.")
    void createFoodItem_shouldCreateCompositeFoodItemAndCalculateNutrients() {
        when(foodItemDao.findById(1L)).thenReturn(mockFindById(1L));
        when(foodItemDao.findById(2L)).thenReturn(mockFindById(2L));
        when(foodItemDao.findById(3L)).thenReturn(mockFindById(3L));

        List<FoodComponent> components = List.of(
                new FoodComponent(2L, 100),
                new FoodComponent(3L, 500)

        );

        FoodItem item = new FoodItem.Builder()
                .setName("Пирог")
                .setCaloriesPer100g(0.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.SLICE)
                .setProteinsPer100g(0.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(0.0)
                .setComposite(true)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(components)
                .build();
        FoodItem savedItem = new FoodItem.Builder()
                .setId(100L)
                .setName("Пирог")
                .setCaloriesPer100g(0.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.SLICE)
                .setProteinsPer100g(0.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(0.0)
                .setComposite(true)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(components)
                .build();

        when(foodItemDao.findByName("Пирог")).thenReturn(Optional.empty());
        when(foodItemDao.save(item)).thenReturn(savedItem);
        FoodItem createdItem = foodItemService.createFoodItem(item);

        assertNotNull(createdItem);
        assertEquals(100L, createdItem.getId(), "ID должен быть присвоен после операции создания.");
        assertEquals("Пирог", createdItem.getName(), "После операции создания мы должны получить обратно продукт с тем же именем.");
        assertEquals(331.25, createdItem.getCaloriesPer100g(), 0.001, "Значение калорийности должно корректно рассчитаться для составного продукта.");
        assertEquals(6.25, createdItem.getProteinsPer100g(), 0.001, "Значение белков должно корректно рассчитаться для составного продукта.");
        assertEquals(0.625, createdItem.getFatsPer100g(), 0.001, "Значение жиров должно корректно рассчитаться для составного продукта.");
        assertEquals(81.25, createdItem.getCarbsPer100g(), 0.001, "Значение углеводов должно корректно рассчитаться для составного продукта.");
        verify(foodItemDao).findByName("Пирог");
        verify(foodItemDao).save(item);
        verify(foodItemDao, times(6)).findById(anyLong());
        verify(foodItemDao, times(2)).findById(1L);
        verify(foodItemDao, times(2)).findById(2L);
        verify(foodItemDao, times(2)).findById(3L);
    }

    @Test
    @DisplayName("ServiceException при вызове метода createFoodItem на FoodItem с неуникальным именем.")
    void createFoodItem_shouldThrowServiceExceptionOnDuplicateName() {
        FoodItem existingItem = new FoodItem.Builder()
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
        when(foodItemDao.findByName("Говядина")).thenReturn(Optional.of(existingItem));

        FoodItem newItem = new FoodItem.Builder()
                .setName("Говядина")
                .setCaloriesPer100g(260.0)
                .setServingSizeInGrams(400.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(18.0)
                .setFatsPer100g(17.0)
                .setCarbsPer100g(1.5)
                .setComposite(false)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(null)
                .build();

        ServiceException exception = assertThrows(ServiceException.class, () ->foodItemService.createFoodItem(newItem),
                "Должно быть ServiceException при вызове метода createFoodItem на FoodItem с неуникальным именем.");
        assertEquals("Продукт с именем Говядина уже существует.", exception.getMessage(),
                "Сообщение об ошибке должно указывать на проблему с именем.");
        verify(foodItemDao).findByName("Говядина");
        verify(foodItemDao, never()).save(any(FoodItem.class));
    }

    @Test
    @DisplayName("ServiceException при вызове метода createFoodItem на FoodItem с циклической зависимостью.")
    void createFoodItem_shouldThrowServiceExceptionOnCyclicDependency() {
        FoodItem cyclicItem = new FoodItem.Builder()
                .setId(100L)
                .setName("Тесто")
                .setCaloriesPer100g(0.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(0.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(0.0)
                .setComposite(true)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(List.of(new FoodComponent(100L, 100.0)))
                .build();

        when(foodItemDao.findByName(anyString())).thenReturn(Optional.empty());
        when(foodItemDao.findById(100L)).thenReturn(Optional.of(cyclicItem));


        ServiceException exception = assertThrows(ServiceException.class, () -> foodItemService.createFoodItem(cyclicItem),
                "Должно быть ServiceException при вызове метода createFoodItem на FoodItem с циклической зависимостью.");
        assertTrue(exception.getMessage().contains("Обнаружена циклическая зависимость: продукт Тесто"),
                "Сообщение об ошибке должно указывать на проблему с циклической зависимостью.");
        verify(foodItemDao).findByName("Тесто");
        verify(foodItemDao, times(1)).findById(100L);
        verify(foodItemDao, never()).save(any(FoodItem.class));
    }

    @Test
    @DisplayName("ServiceException при вызове метода createFoodItem на FoodItem с несуществующим ингредиентом.")
    void createFoodItem_shouldThrowServiceExceptionOnNonExistentIngredient() {
        FoodItem itemWithNonExistentIngredient = new FoodItem.Builder()
                .setName("Тесто")
                .setCaloriesPer100g(0.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(0.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(0.0)
                .setComposite(true)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(List.of(new FoodComponent(999L, 100.0)))
                .build();

        when(foodItemDao.findByName(anyString())).thenReturn(Optional.empty());
        when(foodItemDao.findById(999L)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> foodItemService.createFoodItem(itemWithNonExistentIngredient),
                "Должно быть ServiceException при вызове метода createFoodItem на FoodItem с несуществующим ингредиентом.");

        assertEquals("Ингредиент с ID 999 не найден.", exception.getMessage(),
                "Сообщение об ошибке должно указывать на проблему с ненайденным ингредиентом.");
        verify(foodItemDao).findById(999L);
        verify(foodItemDao, never()).save(any(FoodItem.class));
    }

    @Test
    @DisplayName("ServiceException при ошибке DAO во время вызова метода createFoodItem.")
    void createFoodItem_shouldThrowServiceExceptionOnDataAccessException() {
        FoodItem item = new FoodItem.Builder()
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
        when(foodItemDao.findByName(anyString())).thenReturn(Optional.empty());
        when(foodItemDao.save(item)).thenThrow(new DataAccessException("Ошибка БД."));

        ServiceException exception = assertThrows(ServiceException.class, () -> foodItemService.createFoodItem(item),
                "Должно быть ServiceException при ошибке DAO во время вызова метода createFoodItem.");
        assertTrue(exception.getMessage().contains("Не удалось создать продукт Говядина"),
                "Сообщение об ошибке должно указывать на проблему с созданием продукта.");
        verify(foodItemDao).save(item);
    }

    @Test
    @DisplayName("Метод getFoodItemById должен вернуть простой существующий FoodItem по ID.")
    void getFoodItemById_shouldReturnExistingNonCompositeFoodItem() {
        FoodItem savedItem = new FoodItem.Builder()
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

        when(foodItemDao.findById(100L)).thenReturn(Optional.of(savedItem));

        Optional<FoodItem> foundItem = foodItemService.getFoodItemById(100L);

        assertTrue(foundItem.isPresent(), "FoodItem должен быть найден.");
        assertEquals(100L, foundItem.get().getId(), "ID найденного FoodItem должно совпадать.");
        assertEquals("Говядина", foundItem.get().getName(), "Имя найденного FoodItem должно совпадать.");
        assertEquals(250.0, foundItem.get().getCaloriesPer100g(), 0.001, "Калорийность найденного FoodItem должна совпадать.");
        assertEquals(19.0, foundItem.get().getProteinsPer100g(), 0.001, "Белки найденного FoodItem должны совпадать.");
        assertEquals(16.0, foundItem.get().getFatsPer100g(), 0.001, "Жиры найденного FoodItem должны совпадать.");
        assertEquals(1.0, foundItem.get().getCarbsPer100g(), 0.001, "Углеводы найденного FoodItem должны совпадать.");
        verify(foodItemDao).findById(100L);
    }

    @Test
    @DisplayName("Метод getFoodItemById должен вернуть составной существующий FoodItem и расчитать нутриенты.")
    void getFoodItemById_shouldReturnExistingCompositeFoodItemAndCalculateNutrients() {
        when(foodItemDao.findById(1L)).thenReturn(mockFindById(1L));
        when(foodItemDao.findById(2L)).thenReturn(mockFindById(2L));
        when(foodItemDao.findById(3L)).thenReturn(mockFindById(3L));

        List<FoodComponent> components = List.of(
                new FoodComponent(2L, 100),
                new FoodComponent(3L, 500)

        );

        FoodItem savedItem = new FoodItem.Builder()
                .setId(100L)
                .setName("Пирог")
                .setCaloriesPer100g(0.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.SLICE)
                .setProteinsPer100g(0.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(0.0)
                .setComposite(true)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(components)
                .build();

        when(foodItemDao.findById(100L)).thenReturn(Optional.of(savedItem));

        Optional<FoodItem> foundItem = foodItemService.getFoodItemById(100L);

        assertTrue(foundItem.isPresent(), "FoodItem должен быть найден.");
        assertEquals(100L, foundItem.get().getId(), "ID найденного FoodItem должно совпадать.");
        assertEquals("Пирог", foundItem.get().getName(), "Имя найденного FoodItem должно совпадать.");
        assertEquals(331.25, foundItem.get().getCaloriesPer100g(), 0.001, "Значение калорийности должно корректно рассчитаться для составного продукта.");
        assertEquals(6.25, foundItem.get().getProteinsPer100g(), 0.001, "Значение белков должно корректно рассчитаться для составного продукта.");
        assertEquals(0.625, foundItem.get().getFatsPer100g(), 0.001, "Значение жиров должно корректно рассчитаться для составного продукта.");
        assertEquals(81.25, foundItem.get().getCarbsPer100g(), 0.001, "Значение углеводов должно корректно рассчитаться для составного продукта.");
        verify(foodItemDao, times(4)).findById(anyLong());
        verify(foodItemDao, times(1)).findById(100L);
        verify(foodItemDao, times(1)).findById(1L);
        verify(foodItemDao, times(1)).findById(2L);
        verify(foodItemDao, times(1)).findById(3L);
    }

    @Test
    @DisplayName("Метод getFoodItemById должен вернуть вернуть Optional.empty, если food item не существует.")
    void getFoodItemById_shouldReturnEmptyForNonExistentFoodItem() {
        when(foodItemDao.findById(999L)).thenReturn(Optional.empty());

        Optional<FoodItem> foundItem = foodItemService.getFoodItemById(999L);

        assertFalse(foundItem.isPresent(), "FoodItem не должен быть найден.");
        verify(foodItemDao).findById(999L);
    }

    @Test
    @DisplayName("ServiceException при ошибке DAO во время вызова метода getFoodItemById.")
    void getFoodItemById_shouldThrowServiceExceptionOnDataAccessException() {
        when(foodItemDao.findById(100L)).thenThrow(new DataAccessException("Ошибка БД."));

        ServiceException exception = assertThrows(ServiceException.class, () -> foodItemService.getFoodItemById(100L),
                "Должно быть ServiceException при ошибке DAO во время вызова метода getFoodItemById.");
        assertTrue(exception.getMessage().contains("Не удалось получить продукт по ID 100"),
                "Сообщение об ошибке должно указывать на проблему с поиском продукта.");
        verify(foodItemDao).findById(100L);
    }
}