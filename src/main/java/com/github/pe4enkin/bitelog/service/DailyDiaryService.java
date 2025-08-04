package com.github.pe4enkin.bitelog.service;

import com.github.pe4enkin.bitelog.model.DailyDiary;
import com.github.pe4enkin.bitelog.model.MealEntry;

import java.time.LocalDate;
import java.util.List;

public class DailyDiaryService {
    private final MealEntryService mealEntryService;

    public DailyDiaryService(MealEntryService mealEntryService){
        this.mealEntryService = mealEntryService;
    }

    public DailyDiary getDiaryForDate(LocalDate date) {
        List<MealEntry> mealEntries = mealEntryService.getAllByDate(date);
        DailyDiary dailyDiary = new DailyDiary(date, mealEntries);
        dailyDiary.calculateAndSetAllNutrients();

        return dailyDiary;
    }
}