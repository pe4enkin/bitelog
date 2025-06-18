package com.github.pe4enkin.bitelog.model;

import java.util.Objects;

public class MealComponent {
    private long id;
    private FoodItem foodItem;
    private double amountInGrams;

    public MealComponent() {
    }

    public MealComponent(long id, FoodItem foodItem, double amountInGrams) {
        this.id = id;
        this.foodItem = foodItem;
        this.amountInGrams = amountInGrams;
    }

    public long getId() {
        return id;
    }

    public MealComponent setId(long id) {
        this.id = id;
        return this;
    }

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public MealComponent setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
        return this;
    }

    public double getAmountInGrams() {
        return amountInGrams;
    }

    public MealComponent setAmountInGrams(double amountInGrams) {
        this.amountInGrams = amountInGrams;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof MealComponent that)) return false;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MealComponent{" +
                "id=" + id +
                ", foodItem=" + foodItem +
                ", amountInGrams=" + amountInGrams +
                '}';
    }
}
