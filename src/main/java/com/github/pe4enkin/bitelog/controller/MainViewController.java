package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.AppState;
import com.github.pe4enkin.bitelog.service.DiaryService;
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
    private final DiaryService diaryService;

    public MainViewController(AppState appState, DiaryService diaryService) {
        this.appState = appState;
        this.diaryService = diaryService;
    }

    @FXML
    public void initialize() {
        datePicker.setValue(appState.getCurrentWorkingDate());
        appState.currentWorkingDateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (!datePicker.getValue().equals(newValue)) {
                    datePicker.setValue(newValue);
                }
                diaryService.loadForDate(newValue);
            }
        });

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !appState.getCurrentWorkingDate().equals(newValue)) {
                appState.setCurrentWorkingDate(newValue);
            }
        });

        //Загрузка данных при инициализации приложения
        diaryService.loadForDate(appState.getCurrentWorkingDate());
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
}