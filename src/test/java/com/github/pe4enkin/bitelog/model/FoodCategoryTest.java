package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FoodCategoryTest {

    @Test
    @DisplayName("Проверка equals и hashCode при равенстве ID")
    void testEqualsAndHashCodeEqualById () {
        FoodCategory foodCategory1 = new FoodCategory(1, "Мясо");
        FoodCategory foodCategory2 = new FoodCategory(1, "Мясо");
        FoodCategory foodCategory3 = new FoodCategory(1, "Не мясо");

        assertTrue(foodCategory1.equals(foodCategory1));
        assertEquals(foodCategory1.hashCode(), foodCategory1.hashCode());

        assertTrue(foodCategory1.equals(foodCategory2));
        assertTrue(foodCategory2.equals(foodCategory1));
        assertEquals(foodCategory1.hashCode(), foodCategory2.hashCode());

        assertTrue(foodCategory1.equals(foodCategory2));
        assertTrue(foodCategory2.equals(foodCategory3));
        assertTrue(foodCategory1.equals(foodCategory3));
        assertEquals(foodCategory1.hashCode(), foodCategory3.hashCode());
    }

    @Test
    @DisplayName("Проверка equals и hashCode при разных ID")
    void testEqualsAndHashCodeNotEqualById() {
        FoodCategory foodCategory1 = new FoodCategory(1, "Мясо");
        FoodCategory foodCategory2 = new FoodCategory(2, "Мясо");
        FoodCategory foodCategory3 = new FoodCategory(0, "Мясо");
        FoodCategory foodCategory4 = new FoodCategory(0, "Мясо");

        assertFalse(foodCategory1.equals(foodCategory2));
        assertFalse(foodCategory2.equals(foodCategory1));
        assertNotEquals(foodCategory1.hashCode(), foodCategory2.hashCode());

        assertFalse(foodCategory1.equals(null));
        assertFalse(foodCategory1.equals("Объект другого типа."));

        assertEquals(0, foodCategory3.hashCode());
        assertFalse(foodCategory1.equals(foodCategory3));
        assertFalse(foodCategory3.equals(foodCategory4));
    }
}
