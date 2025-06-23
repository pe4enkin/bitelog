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

    private Button previousDayButton;

    @FXML
    private Button todayButton;

    @FXML
    private Button nextDayButton;

    @FXML

    private final AppState appState;
    private final DiaryService diaryService;

    public MainViewController(AppState appState, DiaryService diaryService) {
        this.appState = appState;
        this.diaryService = diaryService;
    }

    @FXML
    public void initialize() {
        datePicker.setValue(appState.getCurrentWorkingDate());
        if (diaryService.loadForDate(appState.getCurrentWorkingDate())) {
            //успешная загрузка
        }
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                appState.setCurrentWorkingDate(newValue);
                if (diaryService.loadForDate(appState.getCurrentWorkingDate())) {
                    //успешная загрузка
                }
            }
        });

        appState.currentWorkingDateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !datePicker.getValue().equals(newValue)) {
                datePicker.setValue(newValue);
            }
        });
    }

    @FXML
    private void handlePreviousDayButtonAction() {
        LocalDate previousDay = datePicker.getValue().minusDays(1);
        datePicker.setValue(previousDay);
    }

    @FXML
    private void handleTodayButtonAction() {
        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
    }

    @FXML
    private void handleNextDayButtonAction() {
        LocalDate nextDay = datePicker.getValue().plusDays(1);
        datePicker.setValue(nextDay);
    }
}