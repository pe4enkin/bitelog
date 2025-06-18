package com.github.pe4enkin.bitelog.model;

import java.util.Objects;

public class FoodCategory {
    private long id;
    private String name;

    public FoodCategory() {
    }

    public FoodCategory(long id, String name) {
        this.id = id;
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
        if (!(o instanceof FoodCategory that)) return false;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }

    @Override
    public String toString() {
        return "FoodCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}