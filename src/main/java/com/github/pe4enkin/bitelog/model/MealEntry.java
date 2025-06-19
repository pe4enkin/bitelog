package com.github.pe4enkin.bitelog.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MealEntry {
    private long id;
    private LocalDate date;
    private LocalTime time;
    private MealCategory category;
    private List<MealComponent> components;
    private double totalCalories;
    private String notes;

    public MealEntry(long id, LocalDate date, LocalTime time, MealCategory category, List<MealComponent> components, double totalCalories, String notes) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.category = category;
        this.components = components;
        this.totalCalories = totalCalories;
        this.notes = notes;
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

    public MealCategory getCategory() {
        return category;
    }

    public MealEntry setCategory(MealCategory category) {
        this.category = category;
        return this;
    }

    public List<MealComponent> getComponents() {
        return components;
    }

    public MealEntry setComponents(List<MealComponent> components) {
        this.components = components;
        return this;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public MealEntry setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public MealEntry setNotes(String notes) {
        this.notes = notes;
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
                ", category=" + category +
                ", components=" + components +
                ", totalCalories=" + totalCalories +
                ", notes='" + notes + '\'' +
                '}';
    }
}