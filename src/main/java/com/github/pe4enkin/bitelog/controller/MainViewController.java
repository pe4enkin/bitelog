package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.*;
import com.github.pe4enkin.bitelog.service.DailyDiaryService;
import com.github.pe4enkin.bitelog.service.FoodItemService;
import com.github.pe4enkin.bitelog.service.exception.ServiceException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class MainViewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);

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
    @FXML
    private VBox mealEntriesVBox;

    private final AppState appState;
    private final FoodItemService foodItemService;
    private final DailyDiaryService dailyDiaryService;

    private DoubleProperty totalCalories = new SimpleDoubleProperty(0.0);
    private DoubleProperty totalProteins = new SimpleDoubleProperty(0.0);
    private DoubleProperty totalFats = new SimpleDoubleProperty(0.0);
    private DoubleProperty totalCarbs = new SimpleDoubleProperty(0.0);

    public MainViewController(AppState appState, FoodItemService foodItemService, DailyDiaryService dailyDiaryService) {
        this.appState = appState;
        this.foodItemService = foodItemService;
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
                displayDailyDiary(dailyDiary);
                updateSummaryLabels(dailyDiary);
            }
        });

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !appState.getCurrentWorkingDate().equals(newValue)) {
                appState.setCurrentWorkingDate(newValue);
            }
        });

        //Загрузка данных при инициализации приложения
        DailyDiary dailyDiary = dailyDiaryService.getDiaryForDate(appState.getCurrentWorkingDate());
        displayDailyDiary(dailyDiary);
        updateSummaryLabels(dailyDiary);

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

    private void displayDailyDiary(DailyDiary dailyDiary) {
        mealEntriesVBox.getChildren().clear();

        for (MealEntry mealEntry : dailyDiary.getMealEntries()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/pe4enkin/bitelog/view/meal-entry-template.fxml"));
                TitledPane mealPane = loader.load();
                String title = String.format("%s (%,.0f ккал)", mealEntry.getMealCategory(), mealEntry.getTotalCalories());
                mealPane.setText(title);
                VBox mealComponentsVBox = (VBox) mealPane.getContent();
                mealComponentsVBox.getChildren().clear();
                for (MealComponent mealComponent : mealEntry.getComponents()) {
                    Optional<FoodItem> optionalFoodItem = foodItemService.getFoodItemById(mealComponent.getFoodItemId());
                    if (optionalFoodItem.isPresent()) {
                        Label mealComponentLabel = new Label(
                                String.format("- %s (%,.0f ккал)", optionalFoodItem.get().getName(), mealComponent.getAmountInGrams() / 100 * optionalFoodItem.get().getCaloriesPer100g())
                        );
                        mealComponentsVBox.getChildren().add(mealComponentLabel);
                    } else {
                        LOGGER.warn("FoodItem c ID {} не найден для отображения.", mealComponent.getFoodItemId());
                        throw new ServiceException("Продукт с ID " + mealComponent.getFoodItemId() + " не найден для отображения.");
                    }
                }
                mealEntriesVBox.getChildren().add(mealPane);
            } catch (IOException e) {
                LOGGER.warn("Неожиданная ошибка ввода-вывода", e.getMessage());
            }
        }
    }

    private void updateSummaryLabels(DailyDiary dailyDiary) {
        totalCalories.set(dailyDiary.getTotalCalories());
        totalProteins.set(dailyDiary.getTotalProteins());
        totalFats.set(dailyDiary.getTotalFats());
        totalCarbs.set(dailyDiary.getTotalCarbs());
    }
}