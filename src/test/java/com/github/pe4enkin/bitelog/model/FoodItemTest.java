package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FoodItemTest {

    @Test
    @DisplayName("Проверка Builder'a не составного FoodItem")
    void testBuilderCreatesNonCompositeFoodItemWithAllFields() {
        FoodCategory foodCategory = new FoodCategory(1, "Мясо");
        FoodItem foodItem = new FoodItem.Builder()
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

        assertEquals(1, foodItem.getId());
        assertEquals("Говядина", foodItem.getName());
        assertEquals(250.0, foodItem.getCaloriesPer100g(), 0.001);
        assertEquals(200.0, foodItem.getServingSizeInGrams(), 0.001);
        assertEquals(Unit.GRAM, foodItem.getUnit());
        assertEquals(19.0, foodItem.getProteinsPer100g(), 0.001);
        assertEquals(16.0, foodItem.getFatsPer100g(), 0.001);
        assertEquals(1.0, foodItem.getCarbsPer100g(), 0.001);
        assertEquals(false, foodItem.isComposite());
        assertEquals(foodCategory, foodItem.getFoodCategory());
        assertNull(foodItem.getComponents());
}

    @Test
    @DisplayName("Проверка Builder'a составного FoodItem")
    void testBuilderCreatesCompositeFoodItemWithAllFields() {
        FoodCategory foodCategory = new FoodCategory(2, "Салаты");
        FoodComponent component1 = new FoodComponent(1, 2, 3, 100);
        FoodComponent component2 = new FoodComponent(1, 2, 4, 200);
        List<FoodComponent> components = Arrays.asList(component1, component2);

        FoodItem foodItem = new FoodItem.Builder()
                .setId(2)
                .setName("Салат")
                .setCaloriesPer100g(100.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(3.0)
                .setFatsPer100g(5.0)
                .setCarbsPer100g(15.0)
                .setComposite(true)
                .setFoodCategory(foodCategory)
                .setComponents(components)
                .build();

        assertEquals(2, foodItem.getId());
        assertEquals("Салат", foodItem.getName());
        assertEquals(100.0, foodItem.getCaloriesPer100g(), 0.001);
        assertEquals(200.0, foodItem.getServingSizeInGrams(), 0.001);
        assertEquals(Unit.GRAM, foodItem.getUnit());
        assertEquals(3.0, foodItem.getProteinsPer100g(), 0.001);
        assertEquals(5.0, foodItem.getFatsPer100g(), 0.001);
        assertEquals(15.0, foodItem.getCarbsPer100g(), 0.001);
        assertEquals(true, foodItem.isComposite());
        assertEquals(foodCategory, foodItem.getFoodCategory());
        assertNotNull(foodItem.getComponents());
        assertEquals(components, foodItem.getComponents());
    }

    @Test
    @DisplayName("Проверка equals и hashCode при равенстве ID")
    void testEqualsAndHashCodeEqualById() {
        FoodCategory foodCategory1 = new FoodCategory(1, "Мясо");
        FoodCategory foodCategory2 = new FoodCategory(1, "Салаты");
        FoodComponent component1 = new FoodComponent(1, 2, 3, 100);
        FoodComponent component2 = new FoodComponent(1, 2, 4, 200);
        List<FoodComponent> components = Arrays.asList(component1, component2);
        
        FoodItem foodItem1 = new FoodItem.Builder()
                .setId(1)
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(foodCategory1)
                .setComponents(null)
                .build();

        FoodItem foodItem2 = new FoodItem.Builder()
                .setId(1)
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(foodCategory1)
                .setComponents(null)
                .build();

        FoodItem foodItem3 = new FoodItem.Builder()
                .setId(1)
                .setName("Салат")
                .setCaloriesPer100g(100.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(3.0)
                .setFatsPer100g(5.0)
                .setCarbsPer100g(15.0)
                .setComposite(true)
                .setFoodCategory(foodCategory2)
                .setComponents(components)
                .build();

        assertTrue(foodItem1.equals(foodItem1));
        assertEquals(foodItem1.hashCode(), foodItem1.hashCode());

        assertTrue(foodItem1.equals(foodItem2));
        assertTrue(foodItem2.equals(foodItem1));
        assertEquals(foodItem1.hashCode(), foodItem2.hashCode());

        assertTrue(foodItem2.equals(foodItem3));
        assertTrue(foodItem1.equals(foodItem3));
        assertEquals(foodItem1.hashCode(), foodItem3.hashCode());
    }

    @Test
    @DisplayName("Проверка equals и hashCode при разных ID")
    void testEqualsAndHashCodeNotEqualById() {
        FoodCategory foodCategory = new FoodCategory(1, "Мясо");

        FoodItem foodItem1 = new FoodItem.Builder()
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

        FoodItem foodItem2 = new FoodItem.Builder()
                .setId(2)
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

        FoodItem foodItem3 = new FoodItem.Builder()
                .setId(0)
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

        FoodItem foodItem4 = new FoodItem.Builder()
                .setId(0)
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

        assertFalse(foodItem1.equals(foodItem2));
        assertFalse(foodItem2.equals(foodItem1));
        assertNotEquals(foodItem1.hashCode(), foodItem2.hashCode());

        assertFalse(foodItem1.equals(null));
        assertFalse(foodItem1.equals("Объект другого типа."));

        assertEquals(0, foodItem3.hashCode());
        assertFalse(foodItem1.equals(foodItem3));
        assertFalse(foodItem3.equals(foodItem4));
    }
}