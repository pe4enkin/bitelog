package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MealEntryTest {

    @Test
    @DisplayName("Проверка Builder'a MealEntry без списка MealComponent")
    void testBuilderCreatesMealEntryWithEmptyList() {
        MealEntry mealEntry = new MealEntry.Builder()
                .setId(1)
                .setDate(LocalDate.of(2025, 7, 15))
                .setTime(LocalTime.of(17, 10))
                .setMealCategory(MealCategory.SNACK)
                .setTotalCalories(300.0)
                .setTotalProteins(25.0)
                .setTotalFats(20.0)
                .setTotalCarbs(5.0)
                .setNotes("notes")
                .setComponents(null)
                .build();

        assertEquals(1, mealEntry.getId());
        assertEquals(LocalDate.of(2025, 7, 15), mealEntry.getDate());
        assertEquals(LocalTime.of(17, 10), mealEntry.getTime());
        assertEquals(MealCategory.SNACK, mealEntry.getMealCategory());
        assertEquals(300.0, mealEntry.getTotalCalories(), 0.001);
        assertEquals(25.0, mealEntry.getTotalProteins(), 0.001);
        assertEquals(20.0, mealEntry.getTotalFats(), 0.001);
        assertEquals(5.0, mealEntry.getTotalCarbs(), 0.001);
        assertEquals("notes", mealEntry.getNotes());
        assertTrue(mealEntry.getComponents().isEmpty());
    }

    @Test
    @DisplayName("Проверка Builder'a MealEntry со списком")
    void testBuilderCreatesMealEntryWithAllFields() {
        MealComponent mealComponent1 = new MealComponent(1, 1, 100);
        MealComponent mealComponent2 = new MealComponent(2, 2, 150);
        List<MealComponent> components =List.of(mealComponent1, mealComponent2);

        MealEntry mealEntry = new MealEntry.Builder()
                .setId(1)
                .setDate(LocalDate.of(2025, 7, 15))
                .setTime(LocalTime.of(17, 10))
                .setMealCategory(MealCategory.SNACK)
                .setTotalCalories(300.0)
                .setTotalProteins(25.0)
                .setTotalFats(20.0)
                .setTotalCarbs(5.0)
                .setNotes("notes")
                .setComponents(components)
                .build();

        assertEquals(1, mealEntry.getId());
        assertEquals(LocalDate.of(2025, 7, 15), mealEntry.getDate());
        assertEquals(LocalTime.of(17, 10), mealEntry.getTime());
        assertEquals(MealCategory.SNACK, mealEntry.getMealCategory());
        assertEquals(300.0, mealEntry.getTotalCalories(), 0.001);
        assertEquals(25.0, mealEntry.getTotalProteins(), 0.001);
        assertEquals(20.0, mealEntry.getTotalFats(), 0.001);
        assertEquals(5.0, mealEntry.getTotalCarbs(), 0.001);
        assertEquals("notes", mealEntry.getNotes());
        assertEquals(components, mealEntry.getComponents());
        assertNotSame(components, mealEntry.getComponents());
    }

    @Test
    @DisplayName("Проверка equals и hashCode при равенстве ID")
    void testEqualsAndHashCodeEqualById() {
        MealEntry mealEntry1 = new MealEntry.Builder()
                .setId(1)
                .setDate(LocalDate.of(2025, 6, 26))
                .setTime(LocalTime.of(12, 0))
                .setMealCategory(MealCategory.LUNCH)
                .setTotalCalories(300.0)
                .setTotalProteins(25.0)
                .setTotalFats(20.0)
                .setTotalCarbs(5.0)
                .setNotes("Обед")
                .setComponents(null)
                .build();

        MealEntry mealEntry2 = new MealEntry.Builder()
                .setId(1)
                .setDate(LocalDate.of(2025, 6, 26))
                .setTime(LocalTime.of(12, 0))
                .setMealCategory(MealCategory.LUNCH)
                .setTotalCalories(300.0)
                .setTotalProteins(25.0)
                .setTotalFats(20.0)
                .setTotalCarbs(5.0)
                .setNotes("Обед")
                .setComponents(null)
                .build();

        MealEntry mealEntry3 = new MealEntry.Builder()
                .setId(1)
                .setDate(LocalDate.of(2024, 5, 25))
                .setTime(LocalTime.of(18, 30))
                .setMealCategory(MealCategory.DINNER)
                .setTotalCalories(600.0)
                .setTotalProteins(50.0)
                .setTotalFats(40.0)
                .setTotalCarbs(10.0)
                .setNotes("Ужин")
                .setComponents(null)
                .build();

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
        MealEntry mealEntry1 = new MealEntry.Builder()
                .setId(1)
                .setDate(LocalDate.of(2025, 6, 26))
                .setTime(LocalTime.of(12, 0))
                .setMealCategory(MealCategory.LUNCH)
                .setTotalCalories(300.0)
                .setTotalProteins(25.0)
                .setTotalFats(20.0)
                .setTotalCarbs(5.0)
                .setNotes("Обед")
                .setComponents(null)
                .build();

        MealEntry mealEntry2 = new MealEntry.Builder()
                .setId(2)
                .setDate(LocalDate.of(2025, 6, 26))
                .setTime(LocalTime.of(12, 0))
                .setMealCategory(MealCategory.LUNCH)
                .setTotalCalories(300.0)
                .setTotalProteins(25.0)
                .setTotalFats(20.0)
                .setTotalCarbs(5.0)
                .setNotes("Обед")
                .setComponents(null)
                .build();

        MealEntry mealEntry3 = new MealEntry.Builder()
                .setId(0)
                .setDate(LocalDate.of(2024, 5, 25))
                .setTime(LocalTime.of(18, 30))
                .setMealCategory(MealCategory.DINNER)
                .setTotalCalories(600.0)
                .setTotalProteins(50.0)
                .setTotalFats(40.0)
                .setTotalCarbs(10.0)
                .setNotes("Ужин")
                .setComponents(null)
                .build();

        MealEntry mealEntry4 = new MealEntry.Builder()
                .setId(0)
                .setDate(LocalDate.of(2024, 5, 25))
                .setTime(LocalTime.of(18, 30))
                .setMealCategory(MealCategory.DINNER)
                .setTotalCalories(600.0)
                .setTotalProteins(50.0)
                .setTotalFats(40.0)
                .setTotalCarbs(10.0)
                .setNotes("Ужин")
                .setComponents(null)
                .build();

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