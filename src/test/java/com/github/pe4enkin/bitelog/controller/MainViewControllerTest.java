package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.AppState;
import com.github.pe4enkin.bitelog.model.DailyDiary;
import com.github.pe4enkin.bitelog.service.DailyDiaryService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainViewControllerTest extends ApplicationTest {
    private MainViewController controller;
    private AppState appState;
    @Mock
    private DailyDiaryService dailyDiaryService;
    private DatePicker datePicker;
    private DailyDiary dailyDiary;

    @Override
    public void start(Stage stage) throws Exception {
        appState = new AppState();
        dailyDiary = new DailyDiary(LocalDate.now(), new ArrayList<>());
        when(dailyDiaryService.getDiaryForDate(any(LocalDate.class))).thenReturn(dailyDiary);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/pe4enkin/bitelog/view/main-view.fxml"));
        loader.setControllerFactory(type -> {
            if (type == MainViewController.class) {
                controller = new MainViewController(appState, dailyDiaryService);
                return controller;
            } else {
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        datePicker = lookup("#datePicker").query();
        assertNotNull(datePicker, "Datepicker должен быть найден по fx:id = 'datePicker'");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(appState.getCurrentWorkingDate(), datePicker.getValue(), "Datepicker должен быть инициализирован текущей датой из AppState.");
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(appState.getCurrentWorkingDate());
        reset(dailyDiaryService);
        when(dailyDiaryService.getDiaryForDate(any(LocalDate.class))).thenReturn(dailyDiary);
    }

    @BeforeEach
    void setUp() throws TimeoutException {
        appState.setCurrentWorkingDate(LocalDate.now());
        reset(dailyDiaryService);
        when(dailyDiaryService.getDiaryForDate(any(LocalDate.class))).thenReturn(dailyDiary);
        interact(() -> {
            datePicker.setValue(appState.getCurrentWorkingDate());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("Обновление AppState при изменении календаря.")
    void datePickerUpdatesAppStateAndLoadsData() {
        LocalDate newDate = appState.getCurrentWorkingDate().plusDays(5);
        interact(() -> datePicker.setValue(newDate));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(newDate, appState.getCurrentWorkingDate());
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(newDate);
    }

    @Test
    @DisplayName("Обновление календаря при изменении AppState.")
    void appStateUpdatesDatePicker() {
        LocalDate newDate = appState.getCurrentWorkingDate().plusDays(5);
        interact(() -> appState.setCurrentWorkingDate(newDate));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(newDate, datePicker.getValue());
    }

    @Test
    @DisplayName("Работа кнопки 'предыдущий день'.")
    void handlePreviousDayButtonActionSetsPreviousDay() throws TimeoutException {
        LocalDate initialDate = appState.getCurrentWorkingDate();
        interact(() -> datePicker.setValue(initialDate));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#previousDayButton");
        LocalDate expectedDate = initialDate.minusDays(1);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> expectedDate.equals(datePicker.getValue()));
        assertEquals(expectedDate, datePicker.getValue());
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(expectedDate);
    }

    @Test
    @DisplayName("Работа кнопки 'сегодня'.")
    void handleTodayButtonActionSetsToday() throws TimeoutException{
        LocalDate initialDate = LocalDate.now().minusDays(5);
        interact(() -> datePicker.setValue(initialDate));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#todayButton");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> LocalDate.now().equals(datePicker.getValue()));
        assertEquals(LocalDate.now(), datePicker.getValue());
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(LocalDate.now());
    }

    @Test
    @DisplayName("Работа кнопки 'следующий день'.")
    void handleNextDayButtonActionSetsNextDay() throws TimeoutException{
        LocalDate initialDate = appState.getCurrentWorkingDate();
        interact(() -> datePicker.setValue(initialDate));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#nextDayButton");
        LocalDate expectedDate = initialDate.plusDays(1);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> expectedDate.equals(datePicker.getValue()));
        assertEquals(expectedDate, datePicker.getValue());
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(expectedDate);
    }
}