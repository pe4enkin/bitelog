package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodComponentTest {

    @Test
    @DisplayName("Проверка equals и hashCode при равенстве ID")
    void testEqualsAndHashCodeEqualById () {
        FoodComponent foodComponent1 = new FoodComponent(1, 2, 3, 100);
        FoodComponent foodComponent2 = new FoodComponent(1, 2, 3, 100);
        FoodComponent foodComponent3 = new FoodComponent(1, 5, 7, 200);

        assertTrue(foodComponent1.equals(foodComponent1));
        assertEquals(foodComponent1.hashCode(), foodComponent1.hashCode());

        assertTrue(foodComponent1.equals(foodComponent2));
        assertTrue(foodComponent2.equals(foodComponent1));
        assertEquals(foodComponent1.hashCode(), foodComponent2.hashCode());

        assertTrue(foodComponent2.equals(foodComponent3));
        assertTrue(foodComponent1.equals(foodComponent3));
        assertEquals(foodComponent1.hashCode(), foodComponent3.hashCode());
    }

    @Test
    @DisplayName("Проверка equals и hashCode при разных ID")
    void testEqualsAndHashCodeNotEqualById() {
        FoodComponent foodComponent1 = new FoodComponent(1, 2, 3, 100);
        FoodComponent foodComponent2 = new FoodComponent(2, 2, 3, 100);
        FoodComponent foodComponent3 = new FoodComponent(0, 2, 3, 100);
        FoodComponent foodComponent4 = new FoodComponent(0, 2, 3, 100);

        assertFalse(foodComponent1.equals(foodComponent2));
        assertFalse(foodComponent2.equals(foodComponent1));
        assertNotEquals(foodComponent1.hashCode(), foodComponent2.hashCode());

        assertFalse(foodComponent1.equals(null));
        assertFalse(foodComponent1.equals("Объект другого типа."));

        assertEquals(0, foodComponent3.hashCode());
        assertFalse(foodComponent1.equals(foodComponent3));
        assertFalse(foodComponent3.equals(foodComponent4));
    }
}