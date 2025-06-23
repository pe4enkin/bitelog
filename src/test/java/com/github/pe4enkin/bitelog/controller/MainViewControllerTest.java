package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.AppState;
import com.github.pe4enkin.bitelog.service.DiaryService;
import javafx.application.Platform;
import javafx.scene.control.DatePicker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MainViewControllerTest {

    private MainViewController controller;
    private AppState appState;
    private DiaryService diaryService;

    private DatePicker datePicker;

    @BeforeEach
    void setUp() throws InterruptedException {
        appState = new AppState();
        diaryService = mock(DiaryService.class);
        controller = new MainViewController(appState, diaryService);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            datePicker = new DatePicker();
            injectField(controller, "datePicker", datePicker);
            latch.countDown();
        });
        latch.await(3, TimeUnit.SECONDS);

        when(diaryService.loadForDate(any(LocalDate.class))).thenReturn(true);

        CountDownLatch initLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.initialize();
            initLatch.countDown();
        });
        initLatch.await(3, TimeUnit.SECONDS);
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Установка даты и загрузка информации при инициализации приложения.")
    void initializeSetsDatePickerAndLoadsData() {
        assertEquals(appState.getCurrentWorkingDate(), datePicker.getValue());
        verify(diaryService, atLeastOnce()).loadForDate(appState.getCurrentWorkingDate());
    }

    @Test
    @DisplayName("Обновление AppState при изменении календаря.")
    void datePickerUpdatesAppStateAndLoadsData() throws InterruptedException {
        LocalDate newDate = appState.getCurrentWorkingDate().plusDays(5);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            datePicker.setValue(newDate);
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
        assertEquals(newDate, appState.getCurrentWorkingDate());
    }

    @Test
    @DisplayName("Обновление календаря при изменении AppState.")
    void appStateUpdatesDatePicker() throws InterruptedException {
        LocalDate newDate = appState.getCurrentWorkingDate().plusDays(5);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            appState.setCurrentWorkingDate(newDate);
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);

        assertEquals(newDate, datePicker.getValue());
    }

    @Test
    @DisplayName("Работа кнопки 'предыдущий день'.")
    void handlePreviousDayButtonActionSetsPreviousDay() throws InterruptedException {

    }

    @Test
    @DisplayName("Работа кнопки 'сегодня'.")
    void handleTodayButtonActionSetsToday() throws InterruptedException {

    }

    @Test
    @DisplayName("Работа кнопки 'следующий день'.")
    void handleNextDayButtonActionSetsNextDay() throws InterruptedException {

    }
}