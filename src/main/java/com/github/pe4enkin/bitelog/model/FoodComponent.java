package com.github.pe4enkin.bitelog.model;

import java.util.Objects;

public class FoodComponent {
    private long id;
    private FoodItem parent;
    private FoodItem ingredient;
    private double amountInGrams;

    public FoodComponent() {
    }

    public FoodComponent(long id, FoodItem parent, FoodItem ingredient, double amountInGrams) {
        this.id = id;
        this.parent = parent;
        this.ingredient = ingredient;
        this.amountInGrams = amountInGrams;
    }

    public long getId() {
        return id;
    }

    public FoodComponent setId(long id) {
        this.id = id;
        return this;
    }

    public FoodItem getParent() {
        return parent;
    }

    public FoodComponent setParent(FoodItem parent) {
        this.parent = parent;
        return this;
    }

    public FoodItem getIngredient() {
        return ingredient;
    }

    public FoodComponent setIngredient(FoodItem ingredient) {
        this.ingredient = ingredient;
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
        if (!(o instanceof FoodComponent that)) return false;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }

    @Override
    public String toString() {
        return "FoodComponent{" +
                "id=" + id +
                ", parent=" + parent +
                ", ingredient=" + ingredient +
                ", amountInGrams=" + amountInGrams +
                '}';
    }
}