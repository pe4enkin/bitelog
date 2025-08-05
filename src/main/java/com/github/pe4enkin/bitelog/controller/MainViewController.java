package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.AppState;
import com.github.pe4enkin.bitelog.model.DailyDiary;
import com.github.pe4enkin.bitelog.service.DailyDiaryService;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDate;

public class MainViewController {
    @FXML
    private DatePicker datePicker;
    @FXML
    private Button previousDayButton;
    @FXML
    private Button todayButton;
    @FXML
    private Button nextDayButton;
    @FXML
    private Label totalCaloriesLabel;
    @FXML
    private Label totalProteinsLabel;
    @FXML
    private Label totalFatsLabel;
    @FXML
    private Label totalCarbsLabel;

    private final AppState appState;
    private final DailyDiaryService dailyDiaryService;

    private DoubleProperty totalCalories = new SimpleDoubleProperty(0.0);
    private DoubleProperty totalProteins = new SimpleDoubleProperty(0.0);
    private DoubleProperty totalFats = new SimpleDoubleProperty(0.0);
    private DoubleProperty totalCarbs = new SimpleDoubleProperty(0.0);

    public MainViewController(AppState appState, DailyDiaryService dailyDiaryService) {
        this.appState = appState;
        this.dailyDiaryService = dailyDiaryService;
    }

    @FXML
    public void initialize() {
        datePicker.setValue(appState.getCurrentWorkingDate());
        appState.currentWorkingDateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (!datePicker.getValue().equals(newValue)) {
                    datePicker.setValue(newValue);
                }
                DailyDiary dailyDiary = dailyDiaryService.getDiaryForDate(newValue);
                updateUIWithDiaryData(dailyDiary);
            }
        });

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !appState.getCurrentWorkingDate().equals(newValue)) {
                appState.setCurrentWorkingDate(newValue);
            }
        });

        //Загрузка данных при инициализации приложения
        DailyDiary dailyDiary = dailyDiaryService.getDiaryForDate(appState.getCurrentWorkingDate());
        updateUIWithDiaryData(dailyDiary);

        totalCaloriesLabel.textProperty().bind(totalCalories.asString("Всего калорий: %,.0f"));
        totalProteinsLabel.textProperty().bind(totalProteins.asString("Всего белков: %,.0f"));
        totalFatsLabel.textProperty().bind(totalFats.asString("Всего жиров: %,.0f"));
        totalCarbsLabel.textProperty().bind(totalCarbs.asString("Всего углеводов: %,.0f"));
    }

    @FXML
    private void handlePreviousDayButtonAction() {
        appState.setCurrentWorkingDate(appState.getCurrentWorkingDate().minusDays(1));
    }

    @FXML
    private void handleTodayButtonAction() {
        appState.setCurrentWorkingDate(LocalDate.now());
    }

    @FXML
    private void handleNextDayButtonAction() {
        appState.setCurrentWorkingDate(appState.getCurrentWorkingDate().plusDays(1));
    }

    private void updateUIWithDiaryData(DailyDiary dailyDiary) {
        totalCalories.set(dailyDiary.getTotalCalories());
        totalProteins.set(dailyDiary.getTotalProteins());
        totalFats.set(dailyDiary.getTotalFats());
        totalCarbs.set(dailyDiary.getTotalCarbs());
    }
}