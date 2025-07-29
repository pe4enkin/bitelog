package com.github.pe4enkin.bitelog.service;

import com.github.pe4enkin.bitelog.dao.MealEntryDao;
import com.github.pe4enkin.bitelog.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class MealEntryServiceTest {

    @Mock
    private MealEntryDao mealEntryDao;

    @Mock
    private FoodItemService foodItemService;

    @InjectMocks
    private MealEntryService mealEntryService;

    private FoodItem foodItem1;
    private FoodItem foodItem2;
    private MealComponent mealComponent1;
    private MealComponent mealComponent2;
    private MealEntry mealEntry;

    @BeforeEach
    void setUp() {
        FoodItem foodItem1 = new FoodItem.Builder()
                .setId(100L)
                .setName("Говядина")
                .setCaloriesPer100g(250.0)
                .setServingSizeInGrams(200.0)
                .setUnit(Unit.GRAM)
                .setProteinsPer100g(19.0)
                .setFatsPer100g(16.0)
                .setCarbsPer100g(1.0)
                .setComposite(false)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(null)
                .build();

        FoodItem foodItem2 = new FoodItem.Builder()
                .setId(101L)
                .setName("Курица")
                .setCaloriesPer100g(190.0)
                .setServingSizeInGrams(350.0)
                .setUnit(Unit.PACK)
                .setProteinsPer100g(16.0)
                .setFatsPer100g(14.0)
                .setCarbsPer100g(0.5)
                .setComposite(false)
                .setFoodCategory(new FoodCategory(1L, "Еда"))
                .setComponents(null)
                .build();

        mealComponent1 = new MealComponent(1L, 100L, 50);
        mealComponent2 = new MealComponent(2L, 101L, 300);
        mealEntry = new MealEntry.Builder()
                .setDate(LocalDate.of(2025, 07, 29))
                .setTime(LocalTime.of(19, 30))
                .setMealCategory(MealCategory.DINNER)
                .setTotalCalories(0.0)
                .setTotalProteins(0.0)
                .setTotalFats(0.0)
                .setTotalCarbs(0.0)
                .setNotes("notes")
                .setComponents(List.of(mealComponent1, mealComponent2))
                .build();
    }


}
