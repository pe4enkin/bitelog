package com.github.pe4enkin.bitelog.model;

public class FoodComponent {
    private long id;
    private long ingredientFoodItemId;
    private double amountInGrams;

    public FoodComponent(long id, long ingredientFoodItemId, double amountInGrams) {
        this.id = id;
        this.ingredientFoodItemId = ingredientFoodItemId;
        this.amountInGrams = amountInGrams;
    }

    public FoodComponent(long ingredientFoodItemId, double amountInGrams) {
        this.ingredientFoodItemId = ingredientFoodItemId;
        this.amountInGrams = amountInGrams;
    }

    public long getId() {
        return id;
    }

    public FoodComponent setId(long id) {
        this.id = id;
        return this;
    }

    public long getIngredientFoodItemId() {
        return ingredientFoodItemId;
    }

    public FoodComponent setIngredientFoodItemId(long ingredientFoodItemId) {
        this.ingredientFoodItemId = ingredientFoodItemId;
        return this;
    }

    public double getAmountInGrams() {
        return amountInGrams;
    }

    public FoodComponent setAmountInGrams(double amountInGrams) {
        this.amountInGrams = amountInGrams;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodComponent foodComponent)) return false;
        return getId() != 0 && getId() == foodComponent.getId();
    }

    @Override
    public int hashCode() {
        return getId() != 0 ? Long.hashCode(getId()) : 0;
    }

    @Override
    public String toString() {
        return "FoodComponent{" +
                "id=" + id +
                ", ingredientFoodItemId=" + ingredientFoodItemId +
                ", amountInGrams=" + amountInGrams +
                '}';
    }
}