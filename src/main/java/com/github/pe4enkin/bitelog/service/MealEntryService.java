package com.github.pe4enkin.bitelog.service;

import com.github.pe4enkin.bitelog.dao.MealEntryDao;
import com.github.pe4enkin.bitelog.dao.exception.DataAccessException;
import com.github.pe4enkin.bitelog.model.FoodComponent;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.model.MealComponent;
import com.github.pe4enkin.bitelog.model.MealEntry;
import com.github.pe4enkin.bitelog.service.exception.ServiceException;
import com.github.pe4enkin.bitelog.util.DateTimeFormatterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

public class MealEntryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MealEntryService.class);
    private final MealEntryDao mealEntryDao;
    private final FoodItemService foodItemService;

    public MealEntryService(MealEntryDao mealEntryDao, FoodItemService foodItemService) {
        this.mealEntryDao = mealEntryDao;
        this.foodItemService = foodItemService;
    }

    private void calculateAndSetAllNutrients(MealEntry mealEntry) {
        if (mealEntry.getComponents().isEmpty()) {
            mealEntry.setTotalCalories(0.0);
            mealEntry.setTotalProteins(0.0);
            mealEntry.setTotalFats(0.0);
            mealEntry.setTotalCarbs(0.0);
            return;
        }
        double totalCalories = 0.0;
        double totalProteins = 0.0;
        double totalFats = 0.0;
        double totalCarbs = 0.0;
        for (MealComponent component : mealEntry.getComponents()) {
            Optional<FoodItem> foodItemOptional = foodItemService.getFoodItemById(component.getFoodItemId());
            if (foodItemOptional.isEmpty()) {
                LOGGER.error("FoodItem с ID {} не найден при расчете нутриентов для MealComponent.", component.getFoodItemId());
                throw new ServiceException("Не удалось рассчитать нутриенты: продукт с ID " + component.getFoodItemId() + " не найден.");
            }
            FoodItem foodItem = foodItemOptional.get();
            double scaleFactor = component.getAmountInGrams() / 100.0;
            totalCalories += foodItem.getCaloriesPer100g() * scaleFactor;
            totalProteins += foodItem.getProteinsPer100g() * scaleFactor;
            totalFats += foodItem.getFatsPer100g() * scaleFactor;
            totalCarbs += foodItem.getCarbsPer100g() * scaleFactor;
        }
        mealEntry.setTotalCalories(totalCalories);
        mealEntry.setTotalProteins(totalProteins);
        mealEntry.setTotalFats(totalFats);
        mealEntry.setTotalCarbs(totalCarbs);
    }

    public MealEntry createMealEntry(MealEntry mealEntry) {
        String logMealDateTime = DateTimeFormatterUtil.formatDateTime(mealEntry.getDate(), mealEntry.getTime());
        try {
            MealEntry resultMealEntry = mealEntryDao.save(mealEntry);
            calculateAndSetAllNutrients(resultMealEntry);
            return resultMealEntry;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при создании MealEntry от {}: {}", logMealDateTime, e.getMessage());
            throw new ServiceException("Не удалось создать MealEntry от " + logMealDateTime + ": " + e.getMessage(), e);
        }
    }

    public Optional<MealEntry> getMealEntryById(long id) {
        try {
            Optional<MealEntry> mealEntryOptional = mealEntryDao.findById(id);
            mealEntryOptional.ifPresent(mealEntry -> calculateAndSetAllNutrients(mealEntry));
            return mealEntryOptional;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при получении MealEntry по ID {}: {}", id, e.getMessage());
            throw new ServiceException("Не удалось получить MealEntry по ID " + id + ": " + e.getMessage(), e);
        }
    }

    public MealEntry updateMealEntry(MealEntry mealEntry) {
        String logMealDateTime = DateTimeFormatterUtil.formatDateTime(mealEntry.getDate(), mealEntry.getTime());
        if (mealEntry.getId() <= 0) {
            LOGGER.warn("Попытка обновить MealEntry без указания действительного ID - {}", mealEntry.getId());
            throw new ServiceException("ID MealEntry должен быть указан для обновления.");
        }
        try {
            boolean updated = mealEntryDao.update(mealEntry);
            if (!updated) {
                LOGGER.warn("MealEntry от {} c ID {} не найден для обновления.", logMealDateTime, mealEntry.getId());
                throw new ServiceException("MealEntry от " + logMealDateTime + " не найден для обновления.");
            }
            calculateAndSetAllNutrients(mealEntry);
            return mealEntry;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при обновлении MealEntry от {}: {}", logMealDateTime, e.getMessage());
            throw new ServiceException("Не удалось обновить MealEntry от " + logMealDateTime + ": " + e.getMessage(), e);
        }
    }

    public boolean deleteMealEntry(long id) {
        try {
            boolean deleted = mealEntryDao.delete(id);
            if (!deleted) {
                LOGGER.warn("MealEntry c ID {} не найден для удаления.", id);
            }
            return deleted;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при удалении MealEntry с ID {}: {}", id, e.getMessage());
            throw new ServiceException("Не удалось удалить MealEntry с ID " + id + ": " + e.getMessage(), e);
        }
    }

    public List<MealEntry> getAllByDate(LocalDate searchDate) {
        try {
            List<MealEntry> mealEntries = mealEntryDao.findAllByDate(searchDate);
            for (MealEntry mealEntry : mealEntries) {
                calculateAndSetAllNutrients(mealEntry);
            }
            return mealEntries;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при получении MealEntries на дату {}: {}", DateTimeFormatterUtil.formatDateWithDots(searchDate), e.getMessage());
            throw new ServiceException("Не удалось получить список MealEntries на дату " + DateTimeFormatterUtil.formatDateWithDots(searchDate) + ": " + e.getMessage(), e);
        }
    }
}