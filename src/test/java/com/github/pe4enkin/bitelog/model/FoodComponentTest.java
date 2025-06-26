package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class FoodComponentTest {

    @Test
    @DisplayName("Проверка equals и hashCode при равенстве ID")
    void testEqualsAndHashCodeEqualById () {
        FoodComponent component1 = new FoodComponent(1, 2, 3, 100);
        FoodComponent component2 = new FoodComponent(1, 2, 3, 100);
        FoodComponent component3 = new FoodComponent(1, 5, 7, 200);

        assertTrue(component1.equals(component1));
        assertEquals(component1.hashCode(), component1.hashCode());

        assertTrue(component1.equals(component2));
        assertTrue(component2.equals(component1));
        assertEquals(component1.hashCode(), component2.hashCode());

        assertTrue(component1.equals(component2));
        assertTrue(component2.equals(component3));
        assertTrue(component1.equals(component3));
        assertEquals(component1.hashCode(), component3.hashCode());
    }

    @Test
    @DisplayName("Проверка equals и hashCode при разных ID")
    void testEqualsAndHashCodeNotEqualById() {
        FoodComponent component1 = new FoodComponent(1, 2, 3, 100);
        FoodComponent component2 = new FoodComponent(2, 2, 3, 100);
        FoodComponent component3 = new FoodComponent(0, 2, 3, 100);
        FoodComponent component4 = new FoodComponent(0, 2, 3, 100);

        assertFalse(component1.equals(component2));
        assertFalse(component2.equals(component1));
        assertNotEquals(component1.hashCode(), component2.hashCode());

        assertFalse(component1.equals(null));
        assertFalse(component1.equals("Объект другого типа."));

        assertEquals(0, component3.hashCode());
        assertFalse(component1.equals(component3));
        assertFalse(component3.equals(component4));
    }
}
