package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.AppState;
import com.github.pe4enkin.bitelog.service.DiaryService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDate;

public class MainViewController {
    @FXML
    private DatePicker dateSelection;

    @FXML
    private Button previousDayButton;

    @FXML
    private Button todayButton;

    @FXML
    private Button nextDayButton;

    @FXML
    private Label label;

    @FXML
    private void handleClick() {
        label.setText(appState.getCurrentWorkingDate().toString());
    }

    private final AppState appState;
    private final DiaryService diaryService;

    public MainViewController(AppState appState, DiaryService diaryService) {
        this.appState = appState;
        this.diaryService = diaryService;
    }

    @FXML
    public void initialize() {
        dateSelection.setValue(appState.getCurrentWorkingDate());
        if (diaryService.loadForDate(appState.getCurrentWorkingDate())) {
            label.setText("Данные загружены на дату " + appState.getCurrentWorkingDate());
        }
        dateSelection.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                appState.setCurrentWorkingDate(newValue);
                if (diaryService.loadForDate(appState.getCurrentWorkingDate())) {
                    label.setText("Данные загружены на дату " + appState.getCurrentWorkingDate());
                }
            }
        });

        appState.currentWorkingDateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !dateSelection.getValue().equals(newValue)) {
                dateSelection.setValue(newValue);
            }
        });
    }

    @FXML
    private void handlePreviousDayButtonAction() {
        LocalDate previousDay = dateSelection.getValue().minusDays(1);
        dateSelection.setValue(previousDay);
    }

    @FXML
    private void handleTodayButtonAction() {
        LocalDate today = LocalDate.now();
        dateSelection.setValue(today);
    }

    @FXML
    private void handleNextDayButtonAction() {
        LocalDate nextDay = dateSelection.getValue().plusDays(1);
        dateSelection.setValue(nextDay);
    }
}
