package com.github.pe4enkin.bitelog.model;

import java.time.LocalDate;
import java.util.List;

public class DailyDiary {
    private final LocalDate date;
    private final List<MealEntry> mealEntries;
    private double totalCalories;
    private double totalProteins;
    private double totalFats;
    private double totalCarbs;

    public DailyDiary(LocalDate date, List<MealEntry> mealEntries) {
        this.date = date;
        this.mealEntries = mealEntries;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<MealEntry> getMealEntries() {
        return mealEntries;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public double getTotalProteins() {
        return totalProteins;
    }

    public double getTotalFats() {
        return totalFats;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public  void calculateAndSetAllNutrients() {
        if (mealEntries.isEmpty()) {
            this.totalCalories = 0.0;
            this.totalProteins = 0.0;
            this.totalFats = 0.0;
            this.totalCarbs = 0.0;
            return;
        }
        this.totalCalories = mealEntries.stream().mapToDouble(MealEntry::getTotalCalories).sum();
        this.totalProteins = mealEntries.stream().mapToDouble(MealEntry::getTotalProteins).sum();
        this.totalFats = mealEntries.stream().mapToDouble(MealEntry::getTotalFats).sum();
        this.totalCarbs = mealEntries.stream().mapToDouble(MealEntry::getTotalCarbs).sum();
    }

    @Override
    public String toString() {
        return "DailyDiary{" +
                "date=" + date +
                ", mealEntries=" + mealEntries +
                ", totalCalories=" + totalCalories +
                ", totalProteins=" + totalProteins +
                ", totalFats=" + totalFats +
                ", totalCarbs=" + totalCarbs +
                '}';
    }
}