package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MealComponentTest {
    @Test
    @DisplayName("Проверка equals и hashCode при равенстве ID")
    void testEqualsAndHashCodeEqualById () {
        MealComponent mealComponent1 = new MealComponent(1, 2, 100);
        MealComponent mealComponent2 = new MealComponent(1, 2, 100);
        MealComponent mealComponent3 = new MealComponent(1, 3, 200);

        assertTrue(mealComponent1.equals(mealComponent1));
        assertEquals(mealComponent1.hashCode(), mealComponent1.hashCode());

        assertTrue(mealComponent1.equals(mealComponent2));
        assertTrue(mealComponent2.equals(mealComponent1));
        assertEquals(mealComponent1.hashCode(), mealComponent2.hashCode());

        assertTrue(mealComponent1.equals(mealComponent2));
        assertTrue(mealComponent2.equals(mealComponent3));
        assertTrue(mealComponent1.equals(mealComponent3));
        assertEquals(mealComponent1.hashCode(), mealComponent3.hashCode());
    }

    @Test
    @DisplayName("Проверка equals и hashCode при разных ID")
    void testEqualsAndHashCodeNotEqualById() {
        MealComponent mealComponent1 = new MealComponent(1, 2, 100);
        MealComponent mealComponent2 = new MealComponent(2, 2, 100);
        MealComponent mealComponent3 = new MealComponent(0, 2, 100);
        MealComponent mealComponent4 = new MealComponent(0, 2, 100);

        assertFalse(mealComponent1.equals(mealComponent2));
        assertFalse(mealComponent2.equals(mealComponent1));
        assertNotEquals(mealComponent1.hashCode(), mealComponent2.hashCode());

        assertFalse(mealComponent1.equals(null));
        assertFalse(mealComponent1.equals("Объект другого типа."));

        assertEquals(0, mealComponent3.hashCode());
        assertFalse(mealComponent1.equals(mealComponent3));
        assertFalse(mealComponent3.equals(mealComponent4));
    }
}
