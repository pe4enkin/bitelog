package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.AppState;
import com.github.pe4enkin.bitelog.model.DailyDiary;
import com.github.pe4enkin.bitelog.service.DailyDiaryService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;

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
    private final AppState appState;
    private final DailyDiaryService dailyDiaryService;

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

    }
}