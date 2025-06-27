package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class MealEntryTest {

    @Test
    @DisplayName("Проверка equals и hashCode при равенстве ID")
    void testEqualsAndHashCodeEqualById() {
        MealEntry mealEntry1 = new MealEntry(1,
                LocalDate.of(2025, 6, 26),
                LocalTime.of(12, 0),
                MealCategory.LUNCH,
                Collections.emptyList(),
                500,
                "Обед");

        MealEntry mealEntry2 = new MealEntry(1,
                LocalDate.of(2025, 6, 26),
                LocalTime.of(12, 0),
                MealCategory.LUNCH,
                Collections.emptyList(),
                500,
                "Обед");

        MealEntry mealEntry3 = new MealEntry(1,
                LocalDate.of(2024, 5, 25),
                LocalTime.of(18, 30),
                MealCategory.DINNER,
                Collections.emptyList(),
                750,
                "Ужин");

        assertTrue(mealEntry1.equals(mealEntry1));
        assertEquals(mealEntry1.hashCode(), mealEntry1.hashCode());

        assertTrue(mealEntry1.equals(mealEntry2));
        assertTrue(mealEntry2.equals(mealEntry1));
        assertEquals(mealEntry1.hashCode(), mealEntry2.hashCode());

        assertTrue(mealEntry2.equals(mealEntry3));
        assertTrue(mealEntry1.equals(mealEntry3));
        assertEquals(mealEntry1.hashCode(), mealEntry3.hashCode());
    }

    @Test
    @DisplayName("Проверка equals и hashCode при разных ID")
    void testEqualsAndHashCodeNotEqualById() {
        MealEntry mealEntry1 = new MealEntry(1,
                LocalDate.of(2025, 6, 26),
                LocalTime.of(12, 0),
                MealCategory.LUNCH,
                Collections.emptyList(),
                500,
                "Обед");

        MealEntry mealEntry2 = new MealEntry(2,
                LocalDate.of(2025, 6, 26),
                LocalTime.of(12, 0),
                MealCategory.LUNCH,
                Collections.emptyList(),
                500,
                "Обед");

        MealEntry mealEntry3 = new MealEntry(0,
                LocalDate.of(2024, 5, 25),
                LocalTime.of(18, 30),
                MealCategory.DINNER,
                Collections.emptyList(),
                750,
                "Ужин");

        MealEntry mealEntry4 = new MealEntry(0,
                LocalDate.of(2024, 5, 25),
                LocalTime.of(18, 30),
                MealCategory.DINNER,
                Collections.emptyList(),
                750,
                "Ужин");

        assertFalse(mealEntry1.equals(mealEntry2));
        assertFalse(mealEntry2.equals(mealEntry1));
        assertNotEquals(mealEntry1.hashCode(), mealEntry2.hashCode());

        assertFalse(mealEntry1.equals(null));
        assertFalse(mealEntry1.equals("Объект другого типа."));

        assertEquals(0, mealEntry3.hashCode());
        assertFalse(mealEntry1.equals(mealEntry3));
        assertFalse(mealEntry3.equals(mealEntry4));
    }
}
