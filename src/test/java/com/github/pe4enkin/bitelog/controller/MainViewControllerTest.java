package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.AppState;
import com.github.pe4enkin.bitelog.model.DailyDiary;
import com.github.pe4enkin.bitelog.model.MealEntry;
import com.github.pe4enkin.bitelog.service.DailyDiaryService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private Label totalCaloriesLabel;
    private Label totalProteinsLabel;
    private Label totalFatsLabel;
    private Label totalCarbsLabel;

    @Override
    public void start(Stage stage) throws Exception {
        appState = new AppState();

        when(dailyDiaryService.getDiaryForDate(LocalDate.now())).thenReturn(createTodayDailyDiary());

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
        totalCaloriesLabel = lookup("#totalCaloriesLabel").query();
        assertNotNull(totalCaloriesLabel, "totalCaloriesLabel должен быть найден по fx:id = 'totalCaloriesLabel'");
        totalProteinsLabel = lookup("#totalProteinsLabel").query();
        assertNotNull(totalProteinsLabel, "totalProteinsLabel должен быть найден по fx:id = 'totalProteinsLabel'");
        totalFatsLabel = lookup("#totalFatsLabel").query();
        assertNotNull(totalFatsLabel, "totalFatsLabel должен быть найден по fx:id = 'totalFatsLabel'");
        totalCarbsLabel = lookup("#totalCarbsLabel").query();
        assertNotNull(totalCarbsLabel, "totalCarbsLabel должен быть найден по fx:id = 'totalCarbsLabel'");

        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(appState.getCurrentWorkingDate(), datePicker.getValue(), "Datepicker должен быть инициализирован текущей датой из AppState.");
        assertEquals("Всего калорий: 2 000", totalCaloriesLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего белков: 125", totalProteinsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего жиров: 100", totalFatsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего углеводов: 150", totalCarbsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(appState.getCurrentWorkingDate());
        reset(dailyDiaryService);
    }

    @BeforeEach
    void setUp() throws TimeoutException {
        appState.setCurrentWorkingDate(LocalDate.now());
        interact(() -> {
            datePicker.setValue(appState.getCurrentWorkingDate());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("Обновление AppState при изменении календаря.")
    void datePickerUpdatesAppStateAndLoadsData() {
        when(dailyDiaryService.getDiaryForDate(LocalDate.now().plusDays(5))).thenReturn(createEmptyDailyDiary(LocalDate.now().plusDays(5)));
        LocalDate newDate = appState.getCurrentWorkingDate().plusDays(5);
        interact(() -> datePicker.setValue(newDate));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(newDate, appState.getCurrentWorkingDate());
        assertEquals("Всего калорий: 0", totalCaloriesLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего белков: 0", totalProteinsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего жиров: 0", totalFatsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего углеводов: 0", totalCarbsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(newDate);

    }

    @Test
    @DisplayName("Обновление календаря при изменении AppState.")
    void appStateUpdatesDatePicker() {
        when(dailyDiaryService.getDiaryForDate(LocalDate.now().plusDays(5))).thenReturn(createEmptyDailyDiary(LocalDate.now().plusDays(5)));
        LocalDate newDate = appState.getCurrentWorkingDate().plusDays(5);
        interact(() -> appState.setCurrentWorkingDate(newDate));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(newDate, datePicker.getValue());
        assertEquals("Всего калорий: 0", totalCaloriesLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего белков: 0", totalProteinsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего жиров: 0", totalFatsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего углеводов: 0", totalCarbsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(newDate);
    }

    @Test
    @DisplayName("Работа кнопки 'предыдущий день'.")
    void handlePreviousDayButtonActionSetsPreviousDay() throws TimeoutException {
        when(dailyDiaryService.getDiaryForDate(LocalDate.now().minusDays(1))).thenReturn(createYesterdayDiaryForDate());
        LocalDate initialDate = appState.getCurrentWorkingDate();
        interact(() -> datePicker.setValue(initialDate));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#previousDayButton");
        LocalDate expectedDate = initialDate.minusDays(1);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> expectedDate.equals(datePicker.getValue()));
        assertEquals(expectedDate, datePicker.getValue());
        assertEquals("Всего калорий: 4 000", totalCaloriesLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего белков: 250", totalProteinsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего жиров: 200", totalFatsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего углеводов: 300", totalCarbsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(expectedDate);

    }

    @Test
    @DisplayName("Работа кнопки 'сегодня'.")
    void handleTodayButtonActionSetsToday() throws TimeoutException{
        when(dailyDiaryService.getDiaryForDate(LocalDate.now().plusDays(5))).thenReturn(createEmptyDailyDiary(LocalDate.now().plusDays(5)));
        when(dailyDiaryService.getDiaryForDate(LocalDate.now())).thenReturn(createTodayDailyDiary());
        LocalDate initialDate = LocalDate.now().plusDays(5);
        interact(() -> datePicker.setValue(initialDate));
        assertEquals("Всего калорий: 0", totalCaloriesLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего белков: 0", totalProteinsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего жиров: 0", totalFatsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего углеводов: 0", totalCarbsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#todayButton");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> LocalDate.now().equals(datePicker.getValue()));
        assertEquals(LocalDate.now(), datePicker.getValue());
        assertEquals("Всего калорий: 2 000", totalCaloriesLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего белков: 125", totalProteinsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего жиров: 100", totalFatsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего углеводов: 150", totalCarbsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(LocalDate.now());
    }

    @Test
    @DisplayName("Работа кнопки 'следующий день'.")
    void handleNextDayButtonActionSetsNextDay() throws TimeoutException{
        when(dailyDiaryService.getDiaryForDate(LocalDate.now().plusDays(1))).thenReturn(createEmptyDailyDiary(LocalDate.now().plusDays(1)));
        LocalDate initialDate = appState.getCurrentWorkingDate();
        interact(() -> datePicker.setValue(initialDate));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#nextDayButton");
        LocalDate expectedDate = initialDate.plusDays(1);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> expectedDate.equals(datePicker.getValue()));
        assertEquals(expectedDate, datePicker.getValue());
        assertEquals("Всего калорий: 0", totalCaloriesLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего белков: 0", totalProteinsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего жиров: 0", totalFatsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего углеводов: 0", totalCarbsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(expectedDate);
    }

    private DailyDiary createTodayDailyDiary() {
        MealEntry mealEntry1 = new MealEntry.Builder()
                .setTotalCalories(1050.0)
                .setTotalProteins(24.5)
                .setTotalFats(49.1)
                .setTotalCarbs(99.9)
                .build();

        MealEntry mealEntry2 = new MealEntry.Builder()
                .setTotalCalories(950.0)
                .setTotalProteins(100.5)
                .setTotalFats(50.9)
                .setTotalCarbs(50.1)
                .build();

        List<MealEntry> mealEntries = List.of(mealEntry1, mealEntry2);
        DailyDiary dailyDiary = new DailyDiary(LocalDate.now(), mealEntries);
        dailyDiary.calculateAndSetAllNutrients();
        return dailyDiary;
    }

    private DailyDiary createEmptyDailyDiary(LocalDate date) {
        DailyDiary dailyDiary = new DailyDiary(date, new ArrayList<>());
        dailyDiary.calculateAndSetAllNutrients();
        return dailyDiary;
    }

    private DailyDiary createYesterdayDiaryForDate() {
        MealEntry mealEntry1 = new MealEntry.Builder()
                .setTotalCalories(2100.0)
                .setTotalProteins(49.0)
                .setTotalFats(98.2)
                .setTotalCarbs(199.8)
                .build();

        MealEntry mealEntry2 = new MealEntry.Builder()
                .setTotalCalories(1900.0)
                .setTotalProteins(201.0)
                .setTotalFats(101.8)
                .setTotalCarbs(100.2)
                .build();

        List<MealEntry> mealEntries = List.of(mealEntry1, mealEntry2);
        DailyDiary dailyDiary = new DailyDiary(LocalDate.now().minusDays(1), mealEntries);
        dailyDiary.calculateAndSetAllNutrients();
        return dailyDiary;
    }
}