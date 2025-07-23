package com.github.pe4enkin.bitelog.service;

import com.github.pe4enkin.bitelog.dao.FoodItemDao;
import com.github.pe4enkin.bitelog.dao.exception.DataAccessException;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import com.github.pe4enkin.bitelog.model.FoodComponent;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FoodItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FoodItemService.class);
    private final FoodItemDao foodItemDao;

    public FoodItemService(FoodItemDao foodItemDao) {
        this.foodItemDao = foodItemDao;
    }

    private void checkForComponents(FoodItem foodItem, Long componentId, Set<Long> visitedIds) {
        if (visitedIds.contains(componentId)) {
            LOGGER.error("Обнаружена циклическая зависимость при валидации FoodItem {} c ID {}. Компонент с ID {} уже находится в пути обхода",
                    foodItem.getName(), foodItem.getId(), componentId);
            throw new ServiceException("Обнаружена циклическая зависимость: продукт " + foodItem.getName() +
                    " не может содержать компонент c ID " + componentId + " который косвенно содержит его самого.");
        }
        visitedIds.add(componentId);

        Optional<FoodItem> componentOptional = foodItemDao.findById(componentId);
        if (componentOptional.isEmpty()) {
            LOGGER.error("Попытка создать/изменить составной FoodItem с несуществующим ингредиентом с ID {}", componentId);
            throw new ServiceException("Ингредиент с ID " + componentId + " не найден.");
        }
        FoodItem component = componentOptional.get();

        if (component.isComposite() && component.getComponents() != null) {
            for (FoodComponent subComponent : component.getComponents()) {
                checkForComponents(foodItem, subComponent.getIngredientFoodItemId(), new HashSet<>(visitedIds));
            }
        }
        visitedIds.remove(componentId);
    }

    private void calculateAndSetAllNutrients(FoodItem foodItem, Set<Long> visitedIds) {
        if (!foodItem.isComposite() || foodItem.getComponents() == null || foodItem.getComponents().isEmpty()) {
            return;
        }

        if (visitedIds.contains(foodItem.getId())) {
            LOGGER.error("Обнаружена циклическая зависимость при расчете нутриентов для FoodItem {} c ID {}.",
                    foodItem.getName(), foodItem.getId());
            throw new ServiceException("Не удалось рассчитать нутриенты: обнаружен цикл в определении продукта " + foodItem.getName());
        }
        visitedIds.add(foodItem.getId());

        double totalWeight = 0.0;
        double totalCalories = 0.0;
        double totalProteins = 0.0;
        double totalFats = 0.0;
        double totalCarbs = 0.0;

        for (FoodComponent component : foodItem.getComponents()) {
            Optional<FoodItem> ingredientOptional = getFoodItemById(component.getIngredientFoodItemId());
            if (ingredientOptional.isEmpty()) {
                LOGGER.error("Ингредиент с ID {} не найден при расчете нутриентов для {}.", component.getIngredientFoodItemId(), foodItem.getName());
                throw new ServiceException("Не удалось рассчитать нутриенты: ингредиент с ID " + component.getIngredientFoodItemId() + " не найден.");
            }
            FoodItem ingredient = ingredientOptional.get();

            double scaleFactor = component.getAmountInGrams() / 100.0;
            totalWeight += component.getAmountInGrams();
            totalCalories += ingredient.getCaloriesPer100g() * scaleFactor;
            totalProteins += ingredient.getProteinsPer100g() * scaleFactor;
            totalFats += ingredient.getFatsPer100g() * scaleFactor;
            totalCarbs += ingredient.getCarbsPer100g() * scaleFactor;
        }
        visitedIds.remove(foodItem.getId());

        if (totalWeight > 0) {
            foodItem.setCaloriesPer100g(totalCalories / totalWeight * 100);
            foodItem.setProteinsPer100g(totalProteins / totalWeight * 100);
            foodItem.setFatsPer100g(totalFats / totalWeight * 100);
            foodItem.setCarbsPer100g(totalCarbs / totalWeight * 100);
        } else {
            LOGGER.warn("Составной FoodItem {} c ID {} имеет нулевой вес компонентов. Все нутриенты установлены в 0.", foodItem.getName(), foodItem.getId());
            foodItem.setCaloriesPer100g(0);
            foodItem.setProteinsPer100g(0);
            foodItem.setFatsPer100g(0);
            foodItem.setCarbsPer100g(0);
        }
    }

    public FoodItem createFoodItem(FoodItem foodItem) {
        if (foodItemDao.findByName(foodItem.getName()).isPresent()) {
            LOGGER.warn("Попытка создать FoodItem с уже существующим именем: {}", foodItem.getName());
            throw new ServiceException("Продукт с именем " + foodItem.getName() + " уже существует");
        }

        if (foodItem.isComposite() && foodItem.getComponents() != null) {
            for (FoodComponent component : foodItem.getComponents()) {
                checkForComponents(foodItem, component.getIngredientFoodItemId(), new HashSet<>());
            }
        }
        try {
            FoodItem resultFoodItem = foodItemDao.save(foodItem);
            Set<Long> visitedIds = new HashSet<>();
            calculateAndSetAllNutrients(resultFoodItem, visitedIds);
            return resultFoodItem;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при создании FoodItem {}: {}", foodItem.getName(), e.getMessage());
            throw new ServiceException("Не удалось создать продукт " + foodItem.getName() + ": " + e.getMessage(), e);
        }
    }

    public FoodItem updateFoodItem(FoodItem foodItem) {
        if (foodItem.getId() <= 0) {
            LOGGER.warn("Попытка обновить FoodItem без указания действительного ID - {}", foodItem.getId());
            throw new ServiceException("ID продукта должен быть указан для обновления.");
        }

        Optional<FoodItem> existingFoodItem = foodItemDao.findByName(foodItem.getName());
        if (existingFoodItem.isPresent() && existingFoodItem.get().getId() != foodItem.getId()) {
            LOGGER.warn("Попытка обновить FoodItem с именем, уже существующим у другого продукта - {}", foodItem.getName());
            throw new ServiceException("Продукт с именем " + foodItem.getName() + " уже существует.");
        }

        if (foodItem.isComposite() && foodItem.getComponents() != null) {
            for (FoodComponent component : foodItem.getComponents()) {
                Set<Long> visitedIds = new HashSet<>();
                visitedIds.add(foodItem.getId());
                checkForComponents(foodItem, component.getIngredientFoodItemId(), visitedIds);
            }
        }
        try {
            boolean updated = foodItemDao.update(foodItem);
            if (!updated) {
                LOGGER.warn("FoodItem {} c ID {} не найден для обновления.", foodItem.getName(), foodItem.getId());
                throw new ServiceException("Продукт с именем " + foodItem.getName() + " не найден для обновления.");
            }
            calculateAndSetAllNutrients(foodItem, new HashSet<>());
            return foodItem;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при обновлении FoodItem {}: {}", foodItem.getName(), e.getMessage());
            throw new ServiceException("Не удалось обновить продукт " + foodItem.getName() + ": " + e.getMessage(), e);
        }
    }

    public Optional<FoodItem> getFoodItemById(long id) {
        try {
            Optional<FoodItem> foodItemOptional = foodItemDao.findById(id);
            foodItemOptional.ifPresent(foodItem -> calculateAndSetAllNutrients(foodItem, new HashSet<>()));
            return foodItemOptional;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при получении FoodItem по ID {}: {}", id, e.getMessage());
            throw new ServiceException("Не удалось получить продукт по ID " + id + ": " + e.getMessage(), e);
        }
    }

    public Optional<FoodItem> getFoodItemByName(String name) {
        try {
            Optional<FoodItem> foodItemOptional = foodItemDao.findByName(name);
            foodItemOptional.ifPresent(foodItem -> calculateAndSetAllNutrients(foodItem, new HashSet<>()));
            return foodItemOptional;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при получении FoodItem по имени {}: {}", name, e.getMessage());
            throw new ServiceException("Не удалось получить продукт по имени " + name + ": " + e.getMessage(), e);
        }
    }

    public boolean deleteFoodItem(long id) {
        try {
            boolean deleted = foodItemDao.delete(id);
            if (!deleted) {
                LOGGER.warn("FoodItem c ID {} не найден для удаления.", id);
            }
            return deleted;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при удалении FoodItem с ID {}: {}", id, e.getMessage());
            throw new ServiceException("Не удалось удалить продукт: " + e.getMessage(), e);
        }
    }

    public List<FoodItem> getAllFoodItems(boolean loadComponents) {
        try {
            List<FoodItem> foodItems = foodItemDao.findAll(loadComponents);
            if (loadComponents) {
                for (FoodItem item : foodItems) {
                    if (item.isComposite()) {
                        calculateAndSetAllNutrients(item, new HashSet<>());
                    }
                }
            }
            return foodItems;
        } catch (DataAccessException e) {
            LOGGER.error("Ошибка DAO при получении всех FoodItem: {}", e.getMessage());
            throw new ServiceException("Не удалось получить список всех продуктов: " + e.getMessage(), e);
        }
    }
}