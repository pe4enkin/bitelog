package com.github.pe4enkin.bitelog.controller;

import com.github.pe4enkin.bitelog.model.*;
import com.github.pe4enkin.bitelog.service.DailyDiaryService;
import com.github.pe4enkin.bitelog.service.FoodItemService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    @Mock
    private FoodItemService foodItemService;
    private DatePicker datePicker;
    private Label totalCaloriesLabel;
    private Label totalProteinsLabel;
    private Label totalFatsLabel;
    private Label totalCarbsLabel;

    @Override
    public void start(Stage stage) throws Exception {
        appState = new AppState();

        when(dailyDiaryService.getDiaryForDate(LocalDate.now())).thenReturn(createTodayDailyDiary());
        List<FoodItem> foodItems = createFoodItems();
        when(foodItemService.getFoodItemById(1)).thenReturn(Optional.of(foodItems.get(0)));
        when(foodItemService.getFoodItemById(2)).thenReturn(Optional.of(foodItems.get(1)));
        when(foodItemService.getFoodItemById(3)).thenReturn(Optional.of(foodItems.get(2)));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/pe4enkin/bitelog/view/main-view.fxml"));
        loader.setControllerFactory(type -> {
            if (type == MainViewController.class) {
                controller = new MainViewController(appState, foodItemService, dailyDiaryService);
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
        assertEquals("Всего калорий: 1 400", totalCaloriesLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего белков: 101", totalProteinsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего жиров: 72", totalFatsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего углеводов: 57", totalCarbsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals(2, ((VBox) lookup("#mealEntriesVBox").query()).getChildren().size(),
                "Должно быть 2 записи о приемах пищи.");
        TitledPane firstMealPane = lookup("#mealEntriesVBox > TitledPane").nth(0).query();
        assertEquals("Обед (380 ккал)", firstMealPane.getText().replace('\u00A0', ' '));
        VBox firstMealComponents = (VBox) firstMealPane.getContent();
        assertEquals(2, firstMealComponents.getChildren().size(), "Первый прием пищи должен содержать 2 продукта.");
        Label firstComponentLabel = (Label) firstMealComponents.getChildren().get(0);
        assertEquals("- Говядина (250 ккал)", firstComponentLabel.getText().replace('\u00A0', ' '));
        Label secondComponentLabel = (Label) firstMealComponents.getChildren().get(1);
        assertEquals("- Овощи (130 ккал)", secondComponentLabel.getText().replace('\u00A0', ' '));
        TitledPane secondMealPane = lookup("#mealEntriesVBox > TitledPane").nth(1).query();
        assertEquals("Ужин (1 020 ккал)", secondMealPane.getText().replace('\u00A0', ' '));
        VBox secondMealComponents = (VBox) secondMealPane.getContent();
        assertEquals(2, secondMealComponents.getChildren().size(), "Второй прием пищи должен содержать 2 продукта.");
        Label thirdComponentLabel = (Label) secondMealComponents.getChildren().get(0);
        assertEquals("- Курица (760 ккал)", thirdComponentLabel.getText().replace('\u00A0', ' '));
        Label fourthComponentLabel = (Label) secondMealComponents.getChildren().get(1);
        assertEquals("- Овощи (260 ккал)", fourthComponentLabel.getText().replace('\u00A0', ' '));
        verify(foodItemService, atLeastOnce()).getFoodItemById(anyLong());
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
        assertEquals("Всего калорий: 1 400", totalCaloriesLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего белков: 101", totalProteinsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего жиров: 72", totalFatsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        assertEquals("Всего углеводов: 57", totalCarbsLabel.getText().replace('\u00A0', ' '),
                "Текстовая метка калорий должна корректно установить значение.");
        verify(dailyDiaryService, atLeastOnce()).getDiaryForDate(LocalDate.now());
       // sleep(350000);
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

    private List<FoodItem> createFoodItems() {
        FoodItem foodItem1 = new FoodItem.Builder()
                .setId(1)
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(new FoodCategory(1, "Еда"))
                .setComponents(null)
                .build();

        FoodItem foodItem2 = new FoodItem.Builder()
                .setId(2)
                .setName("Овощи")
                .setCaloriesPer100g(65.0)
                .setServingSizeInGrams(100.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(3.0)
                .setFatsPer100g(0.0)
                .setCarbsPer100g(9.0)
                .setComposite(false)
                .setFoodCategory(new FoodCategory(1, "Еда"))
                .setComponents(null)
                .build();

        FoodItem foodItem3 = new FoodItem.Builder()
                .setId(3)
                .setName("Курица")
                .setCaloriesPer100g(190.0)
                .setServingSizeInGrams(350.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(16.0)
                .setFatsPer100g(14.0)
                .setCarbsPer100g(0.5)
                .setComposite(false)
                .setFoodCategory(new FoodCategory(1, "Еда"))
                .setComponents(null)
                .build();

        return List.of(foodItem1, foodItem2, foodItem3);
    }

    private List<MealComponent> createMealComponents() {
        MealComponent mealComponent1 = new MealComponent(1,100);
        MealComponent mealComponent2 = new MealComponent(2, 200);
        MealComponent mealComponent3 = new MealComponent(3,400);
        MealComponent mealComponent4 = new MealComponent(2, 400);

        return List.of(mealComponent1, mealComponent2, mealComponent3, mealComponent4);
    }

    private List<MealEntry> createMealEntries() {
        List<MealComponent> mealComponents = createMealComponents();
        List<MealComponent> mealComponents1 = List.of(mealComponents.get(0), mealComponents.get(1));
        List<MealComponent> mealComponents2 = List.of(mealComponents.get(2), mealComponents.get(3));

        MealEntry mealEntry1 =  new MealEntry.Builder()
                .setId(1)
                .setDate(LocalDate.now())
                .setTime(LocalTime.of(12, 0))
                .setMealCategory(MealCategory.LUNCH)
                .setTotalCalories(380.0)
                .setTotalProteins(25.0)
                .setTotalFats(16.0)
                .setTotalCarbs(19.0)
                .setNotes("notes1")
                .setComponents(mealComponents1)
                .build();
        MealEntry mealEntry2 =  new MealEntry.Builder()
                .setId(2)
                .setDate(LocalDate.now())
                .setTime(LocalTime.of(18, 0))
                .setMealCategory(MealCategory.DINNER)
                .setTotalCalories(1020.0)
                .setTotalProteins(76.0)
                .setTotalFats(56.0)
                .setTotalCarbs(38.0)
                .setNotes("notes2")
                .setComponents(mealComponents2)
                .build();

        return List.of(mealEntry1, mealEntry2);
    }

    private DailyDiary createTodayDailyDiary() {
        DailyDiary dailyDiary = new DailyDiary(LocalDate.now(), createMealEntries());
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