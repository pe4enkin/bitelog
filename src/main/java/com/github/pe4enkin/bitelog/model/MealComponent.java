package com.github.pe4enkin.bitelog.model;

public class MealComponent {
    private long id;
    private long foodItemId;
    private double amountInGrams;

    public MealComponent(long id, long foodItemId, double amountInGrams) {
        this.id = id;
        this.foodItemId = foodItemId;
        this.amountInGrams = amountInGrams;
    }

    public long getId() {
        return id;
    }

    public MealComponent setId(long id) {
        this.id = id;
        return this;
    }

    public long getFoodItemId() {
        return foodItemId;
    }

    public MealComponent setFoodItemId(long foodItemId) {
        this.foodItemId = foodItemId;
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
        if (this == o) return true;
        if (!(o instanceof MealComponent mealComponent)) return false;
        return getId() != 0 && getId() == mealComponent.getId();
    }

    @Override
    public int hashCode() {
        return getId() != 0 ? Long.hashCode(getId()) : 0;
    }

    @Override
    public String toString() {
        return "MealComponent{" +
                "id=" + id +
                ", foodItemId=" + foodItemId +
                ", amountInGrams=" + amountInGrams +
                '}';
    }
}
