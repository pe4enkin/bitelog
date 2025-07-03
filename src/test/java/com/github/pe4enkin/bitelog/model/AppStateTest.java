package com.github.pe4enkin.bitelog.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppStateTest {
    private AppState appState;

    @BeforeEach
    void setUp() {
        appState = new AppState();
    }

    @Test
    @DisplayName("Инициализация с текущей датой")
    void shouldInitializeWithCurrentDate() {
        assertEquals(LocalDate.now(), appState.getCurrentWorkingDate());
    }

    @Test
    @DisplayName("Обновление рабочей даты")
    void shouldUpdateCurrentWorkingDate() {
        LocalDate testDate = LocalDate.of(2025, 06, 19);
        appState.setCurrentWorkingDate(testDate);
        assertEquals(testDate, appState.getCurrentWorkingDate());
    }

    @Test
    @DisplayName("Уведомление слушателей о изменении рабочей даты")
    void shouldNotifyListenersWhenDateChanges() {
        LocalDate initialDate = appState.getCurrentWorkingDate();
        LocalDate newDate = initialDate.plusDays(1);

        final LocalDate[] captureDate = {null};
        appState.currentWorkingDateProperty().addListener((obs, oldVal, newVal) -> {
            captureDate[0] = newVal;
        });

        appState.setCurrentWorkingDate(newDate);
        assertEquals(newDate, captureDate[0]);
        assertEquals(newDate, appState.getCurrentWorkingDate());
    }
}