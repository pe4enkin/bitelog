package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.AppState;
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

    public MainViewController(AppState appState) {
        this.appState = appState;
    }

    @FXML
    public void initialize() {
        dateSelection.setValue(appState.getCurrentWorkingDate());
        dateSelection.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                appState.setCurrentWorkingDate(newValue);
                label.setText("Рабочая дата " + newValue);
                //loadForDate(newValue) заглушка для обновления данных
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
        label.setText("Кнопка previousDay нажата " + previousDay);
    }

    @FXML
    private void handleTodayButtonAction() {
        LocalDate today = LocalDate.now();
        dateSelection.setValue(today);
        label.setText("Кнопка today нажата " + today);
    }

    @FXML
    private void handleNextDayButtonAction() {
        LocalDate nextDay = dateSelection.getValue().plusDays(1);
        dateSelection.setValue(nextDay);
        label.setText("Кнопка nextDay нажата " + nextDay);
    }

    //loadForDate(appState.getCurrentWorkingDate()); заглушка для стартовой загрузки данных
}
