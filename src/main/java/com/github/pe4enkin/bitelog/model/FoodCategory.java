package com.github.pe4enkin.bitelog.model;

public class FoodCategory {
    private long id;
    private String name;

    public FoodCategory(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public FoodCategory(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public FoodCategory setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public FoodCategory setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodCategory foodCategory)) return false;
        return getId() != 0 && getId() == foodCategory.getId();
    }

    @Override
    public int hashCode() {
        return getId() != 0 ? Long.hashCode(getId()) : 0;
    }

    @Override
    public String toString() {
        return "FoodCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}