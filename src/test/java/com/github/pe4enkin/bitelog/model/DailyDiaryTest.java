package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DailyDiaryTest {

    @Test
    @DisplayName("Метод calculateAndSetAllNutrients должен корректно расчитать нутриенты.")
    void calculateAndSetAllNutrients_shouldCalculateNutrients() {
        MealEntry mealEntry1 = new MealEntry.Builder()
                .setTotalCalories(1050.0)
                .setTotalProteins(24.5)
                .setTotalFats(49.1)
                .setTotalCarbs(99.9)
                .build();

        MealEntry mealEntry2 = new MealEntry.Builder()
                .setTotalCalories(950.0)
                .setTotalProteins(100.5)
                .setTotalFats(50.9)
                .setTotalCarbs(50.1)
                .build();

        List<MealEntry> mealEntries = List.of(mealEntry1, mealEntry2);
        DailyDiary dailyDiary = new DailyDiary(LocalDate.now(), mealEntries);

        dailyDiary.calculateAndSetAllNutrients();

        assertEquals(2000.0, dailyDiary.getTotalCalories(), 0.001, "Значение калорийности должно корректно рассчитаться.");
        assertEquals(125.0, dailyDiary.getTotalProteins(), 0.001, "Значение белков должно корректно рассчитаться.");
        assertEquals(100.0, dailyDiary.getTotalFats(), 0.001, "Значение жиров должно корректно рассчитаться.");
        assertEquals(150.0, dailyDiary.getTotalCarbs(), 0.001, "Значение углеводов должно корректно рассчитаться.");
    }

    @Test
    @DisplayName("Метод calculateAndSetAllNutrients должен корректно установить нутриенты в 0 при пустом списке.")
    void calculateAndSetAllNutrients_shouldSetZeroWithEmptyList() {
        List<MealEntry> emptyList = new ArrayList<>();
        DailyDiary dailyDiary = new DailyDiary(LocalDate.now(), emptyList);

        dailyDiary.calculateAndSetAllNutrients();

        assertEquals(0.0, dailyDiary.getTotalCalories(), 0.001, "Значение калорийности должно быть 0 при пустом списке.");
        assertEquals(0.0, dailyDiary.getTotalProteins(), 0.001, "Значение белков должно быть 0 при пустом списке.");
        assertEquals(0.0, dailyDiary.getTotalFats(), 0.001, "Значение жиров должно быть 0 при пустом списке.");
        assertEquals(0.0, dailyDiary.getTotalCarbs(), 0.001, "Значение углеводов должно быть 0 при пустом списке.");
    }
}