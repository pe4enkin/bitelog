package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FoodCategoryTest {

    @Test
    @DisplayName("Проверка equals и hashCode при равенстве ID")
    void testEqualsAndHashCodeEqualById () {
        FoodCategory category1 = new FoodCategory(1, "Мясо");
        FoodCategory category2 = new FoodCategory(1, "Мясо");
        FoodCategory category3 = new FoodCategory(1, "Не мясо");

        assertTrue(category1.equals(category1));
        assertEquals(category1.hashCode(), category1.hashCode());

        assertTrue(category1.equals(category2));
        assertTrue(category2.equals(category1));
        assertEquals(category1.hashCode(), category2.hashCode());

        assertTrue(category1.equals(category2));
        assertTrue(category2.equals(category3));
        assertTrue(category1.equals(category3));
        assertEquals(category1.hashCode(), category3.hashCode());
    }

    @Test
    @DisplayName("Проверка equals и hashCode при разных ID")
    void testEqualsAndHashCodeNotEqualById() {
        FoodCategory category1 = new FoodCategory(1, "Мясо");
        FoodCategory category2 = new FoodCategory(2, "Мясо");
        FoodCategory category3 = new FoodCategory(0, "Мясо");
        FoodCategory category4 = new FoodCategory(0, "Мясо");

        assertFalse(category1.equals(category2));
        assertFalse(category2.equals(category1));
        assertNotEquals(category1.hashCode(), category2.hashCode());

        assertFalse(category1.equals(null));
        assertFalse(category1.equals("Объект другого типа."));

        assertEquals(0, category3.hashCode());
        assertFalse(category1.equals(category3));
        assertFalse(category3.equals(category4));
    }
}
