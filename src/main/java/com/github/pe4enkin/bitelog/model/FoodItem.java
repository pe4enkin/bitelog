package com.github.pe4enkin.bitelog.model;

import java.util.ArrayList;
import java.util.List;

public class FoodItem {
    private long id;
    private String name;
    private double caloriesPer100g;
    private double servingSizeInGrams;
    private Unit unit;
    private double proteinsPer100g;
    private double fatsPer100g;
    private double carbsPer100g;
    private boolean isComposite;
    private FoodCategory foodCategory;
    private List<FoodComponent> components;

    private FoodItem(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.caloriesPer100g = builder.caloriesPer100g;
        this.servingSizeInGrams = builder.servingSizeInGrams;
        this.unit = builder.unit;
        this.proteinsPer100g = builder.proteinsPer100g;
        this.fatsPer100g = builder.fatsPer100g;
        this.carbsPer100g = builder.carbsPer100g;
        this.isComposite = builder.isComposite;
        this.foodCategory = builder.foodCategory;
        this.components = builder.components;
    }

    public long getId() {
        return id;
    }

    public FoodItem setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public FoodItem setName(String name) {
        this.name = name;
        return this;
    }

    public double getCaloriesPer100g() {
        return caloriesPer100g;
    }

    public FoodItem setCaloriesPer100g(double caloriesPer100g) {
        this.caloriesPer100g = caloriesPer100g;
        return this;
    }

    public double getServingSizeInGrams() {
        return servingSizeInGrams;
    }

    public FoodItem setServingSizeInGrams(double servingSizeInGrams) {
        this.servingSizeInGrams = servingSizeInGrams;
        return this;
    }

    public Unit getUnit() {
        return unit;
    }

    public FoodItem setUnit(Unit unit) {
        this.unit = unit;
        return this;
    }

    public double getProteinsPer100g() {
        return proteinsPer100g;
    }

    public FoodItem setProteinsPer100g(double proteinsPer100g) {
        this.proteinsPer100g = proteinsPer100g;
        return this;
    }

    public double getFatsPer100g() {
        return fatsPer100g;
    }

    public FoodItem setFatsPer100g(double fatsPer100g) {
        this.fatsPer100g = fatsPer100g;
        return this;
    }

    public double getCarbsPer100g() {
        return carbsPer100g;
    }

    public FoodItem setCarbsPer100g(double carbsPer100g) {
        this.carbsPer100g = carbsPer100g;
        return this;
    }

    public boolean isComposite() {
        return isComposite;
    }

    public FoodItem setComposite(boolean composite) {
        isComposite = composite;
        return this;
    }

    public FoodCategory getFoodCategory() {
        return foodCategory;
    }

    public FoodItem setFoodCategory(FoodCategory foodCategory) {
        this.foodCategory = foodCategory;
        return this;
    }

    public List<FoodComponent> getComponents() {
        return components;
    }

    public FoodItem setComponents(List<FoodComponent> components) {
        this.components = components;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodItem foodItem)) return false;
        return getId() != 0 && getId() == foodItem.getId();
    }

    @Override
    public int hashCode() {
        return getId() != 0 ? Long.hashCode(getId()) : 0;
    }

    @Override
    public String toString() {
        return "FoodItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", caloriesPer100g=" + caloriesPer100g +
                ", servingSizeInGrams=" + servingSizeInGrams +
                ", unit=" + unit +
                ", proteinsPer100g=" + proteinsPer100g +
                ", fatsPer100g=" + fatsPer100g +
                ", carbsPer100g=" + carbsPer100g +
                ", isComposite=" + isComposite +
                ", foodCategory=" + foodCategory +
                ", components=" + components +
                '}';
    }

    public static class Builder {
        private long id;
        private String name;
        private double caloriesPer100g;
        private double servingSizeInGrams;
        private Unit unit;
        private double proteinsPer100g;
        private double fatsPer100g;
        private double carbsPer100g;
        private boolean isComposite;
        private FoodCategory foodCategory;
        private List<FoodComponent> components;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCaloriesPer100g(double caloriesPer100g) {
            this.caloriesPer100g = caloriesPer100g;
            return this;
        }

        public Builder setServingSizeInGrams(double servingSizeInGrams) {
            this.servingSizeInGrams = servingSizeInGrams;
            return this;
        }

        public Builder setUnit(Unit unit) {
            this.unit = unit;
            return this;
        }

        public Builder setProteinsPer100g(double proteinsPer100g) {
            this.proteinsPer100g = proteinsPer100g;
            return this;
        }

        public Builder setFatsPer100g(double fatsPer100g) {
            this.fatsPer100g = fatsPer100g;
            return this;
        }

        public Builder setCarbsPer100g(double carbsPer100g) {
            this.carbsPer100g = carbsPer100g;
            return this;
        }

        public Builder setComposite(boolean composite) {
            isComposite = composite;
            return this;
        }

        public Builder setFoodCategory(FoodCategory foodCategory) {
            this.foodCategory = foodCategory;
            return this;
        }

        public Builder setComponents(List<FoodComponent> components) {
            this.components = (components != null) ? new ArrayList<>(components) : null;
            return this;
        }

        public FoodItem build() {
            return new FoodItem(this);
        }
    }
}