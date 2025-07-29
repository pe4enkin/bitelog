package com.github.pe4enkin.bitelog.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MealEntry {
    private long id;
    private LocalDate date;
    private LocalTime time;
    private MealCategory mealCategory;
    private double totalCalories;
    private double totalProteins;
    private double totalFats;
    private double totalCarbs;
    private String notes;
    private List<MealComponent> components;

    private MealEntry(Builder builder) {
        this.id = builder.id;
        this.date = builder.date;
        this.time = builder.time;
        this.mealCategory = builder.mealCategory;
        this.totalCalories = builder.totalCalories;
        this.totalProteins = builder.totalProteins;
        this.totalFats = builder.totalFats;
        this.totalCarbs = builder.totalCarbs;
        this.notes = builder.notes;
        this.components = builder.components;
    }

    public long getId() {
        return id;
    }

    public MealEntry setId(long id) {
        this.id = id;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public MealEntry setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public LocalTime getTime() {
        return time;
    }

    public MealEntry setTime(LocalTime time) {
        this.time = time;
        return this;
    }

    public MealCategory getMealCategory() {
        return mealCategory;
    }

    public MealEntry setMealCategory(MealCategory mealCategory) {
        this.mealCategory = mealCategory;
        return this;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public MealEntry setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
        return this;
    }

    public double getTotalProteins() {
        return totalProteins;
    }

    public MealEntry setTotalProteins(double totalProteins) {
        this.totalProteins = totalProteins;
        return this;
    }

    public double getTotalFats() {
        return totalFats;
    }

    public MealEntry setTotalFats(double totalFats) {
        this.totalFats = totalFats;
        return this;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public MealEntry setTotalCarbs(double totalCarbs) {
        this.totalCarbs = totalCarbs;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public MealEntry setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public List<MealComponent> getComponents() {
        return components;
    }

    public MealEntry setComponents(List<MealComponent> components) {
        this.components = components;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MealEntry mealEntry)) return false;
        return getId() != 0 && getId() == mealEntry.getId();
    }

    @Override
    public int hashCode() {
        return getId() != 0 ? Long.hashCode(getId()) : 0;
    }

    @Override
    public String toString() {
        return "MealEntry{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", mealCategory=" + mealCategory +
                ", totalCalories=" + totalCalories +
                ", totalProteins=" + totalProteins +
                ", totalFats=" + totalFats +
                ", totalCarbs=" + totalCarbs +
                ", notes='" + notes + '\'' +
                ", components=" + components +
                '}';
    }

    public static class Builder {
        private long id;
        private LocalDate date;
        private LocalTime time;
        private MealCategory mealCategory;
        private double totalCalories;
        private double totalProteins;
        private double totalFats;
        private double totalCarbs;
        private String notes;
        private List<MealComponent> components = new ArrayList<>();

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder setTime(LocalTime time) {
            this.time = time;
            return this;
        }

        public Builder setMealCategory(MealCategory mealCategory) {
            this.mealCategory = mealCategory;
            return this;
        }

        public Builder setTotalCalories(double totalCalories) {
            this.totalCalories = totalCalories;
            return this;
        }

        public Builder setTotalProteins(double totalProteins) {
            this.totalProteins = totalProteins;
            return this;
        }

        public Builder setTotalFats(double totalFats) {
            this.totalFats = totalFats;
            return this;
        }

        public Builder setTotalCarbs(double totalCarbs) {
            this.totalCarbs = totalCarbs;
            return this;
        }

        public Builder setNotes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder setComponents(List<MealComponent> components) {
            this.components = (components != null) ? new ArrayList<>(components) : new ArrayList<>();
            return this;
        }

        public MealEntry build() {
            return new MealEntry(this);
        }
    }
}