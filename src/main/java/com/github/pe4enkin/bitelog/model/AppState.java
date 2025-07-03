package com.github.pe4enkin.bitelog.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

public class AppState {
    private final ObjectProperty<LocalDate> currentWorkingDate = new SimpleObjectProperty<>();

    public AppState () {
        this.currentWorkingDate.set(LocalDate.now());
    }

    public ObjectProperty<LocalDate> currentWorkingDateProperty() {
        return currentWorkingDate;
    }

    public LocalDate getCurrentWorkingDate() {
        return currentWorkingDate.get();
    }

    public void setCurrentWorkingDate(LocalDate date) {
        this.currentWorkingDate.set(date);
    }
}