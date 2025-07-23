package com.github.pe4enkin.bitelog.service;

import com.github.pe4enkin.bitelog.dao.FoodItemDao;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import com.github.pe4enkin.bitelog.model.FoodComponent;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.model.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @DisplayName("Должен успешно создать простой продукт.")
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
    @DisplayName("Должен успешно создать составной продукт и рассчитать нутриенты.")
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
}
