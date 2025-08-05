package com.github.pe4enkin.bitelog.service;

import com.github.pe4enkin.bitelog.model.DailyDiary;
import com.github.pe4enkin.bitelog.model.MealEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DailyDiaryServiceTest {

    @Mock
    private MealEntryService mealEntryService;

    @InjectMocks
    private DailyDiaryService dailyDiaryService;

    @Test
    @DisplayName("Метод getDiaryForDate должен возвращать корректный DailyDiary.")
    void getDiaryForDate_shouldReturnDailyDiary() {
        LocalDate testDate = LocalDate.of(2025, 8, 5);

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

        when(mealEntryService.getAllByDate(testDate)).thenReturn(mealEntries);

        DailyDiary result = dailyDiaryService.getDiaryForDate(testDate);

        assertNotNull(result);
        assertEquals(testDate, result.getDate(), "DailyDiary должен иметь корректную дату.");
        assertEquals(mealEntries, result.getMealEntries(), "DailyDiary должен иметь корректный список.");
        assertEquals(2000.0, result.getTotalCalories(), 0.001, "Значение калорийности должно корректно рассчитаться.");
        assertEquals(125.0, result.getTotalProteins(), 0.001, "Значение белков должно корректно рассчитаться.");
        assertEquals(100.0, result.getTotalFats(), 0.001, "Значение жиров должно корректно рассчитаться.");
        assertEquals(150.0, result.getTotalCarbs(), 0.001, "Значение углеводов должно корректно рассчитаться.");

        verify(mealEntryService, times(1)).getAllByDate(testDate);
    }

    @Test
    @DisplayName("Метод getDiaryForDate должен возвращать корректный DailyDiary с пустым списком.")
    void getDiaryForDate_shouldReturnDailyDiaryWithEmptyList() {
        LocalDate testDate = LocalDate.of(2025, 8, 5);

        when(mealEntryService.getAllByDate(testDate)).thenReturn(new ArrayList<>());

        DailyDiary result = dailyDiaryService.getDiaryForDate(testDate);

        assertNotNull(result);
        assertEquals(testDate, result.getDate(), "DailyDiary должен иметь корректную дату.");
        assertTrue(result.getMealEntries().isEmpty(), "DailyDiary должен иметь пустой список.");
        assertEquals(0.0, result.getTotalCalories(), 0.001, "Значение калорийности должно быть 0 при пустом списке.");
        assertEquals(0.0, result.getTotalProteins(), 0.001, "Значение белков должно быть 0 при пустом списке.");
        assertEquals(0.0, result.getTotalFats(), 0.001, "Значение жиров должно быть 0 при пустом списке.");
        assertEquals(0.0, result.getTotalCarbs(), 0.001, "Значение углеводов должно быть 0 при пустом списке.");

        verify(mealEntryService, times(1)).getAllByDate(testDate);
    }
}