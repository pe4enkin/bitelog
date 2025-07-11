package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.dao.exception.DataAccessException;
import com.github.pe4enkin.bitelog.dao.util.SqlExceptionTranslator;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import com.github.pe4enkin.bitelog.model.FoodComponent;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.model.Unit;
import com.github.pe4enkin.bitelog.sql.SqlQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodItemDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(FoodItemDao.class);
    private final DataSource dataSource;

    public FoodItemDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createTables() {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute(SqlQueries.CREATE_FOOD_ITEMS_TABLE);
            stmt.execute(SqlQueries.CREATE_FOOD_COMPONENTS_TABLE);
            LOGGER.info("Таблицы food_items и food_components успешно созданы (или уже существовали)");
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при создании таблиц food_items и food_components. SQLState: {}, ErrorCode: {}, message: {}",
                    e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "создании таблиц food_items и food_components");
        }
    }

    public FoodItem save(FoodItem foodItem) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_FOOD_ITEM, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, foodItem.getName());
                pstmt.setDouble(2, foodItem.getCaloriesPer100g());
                pstmt.setDouble(3, foodItem.getServingSizeInGrams());
                pstmt.setString(4, foodItem.getUnit().name());
                pstmt.setDouble(5, foodItem.getProteinsPer100g());
                pstmt.setDouble(6, foodItem.getFatsPer100g());
                pstmt.setDouble(7, foodItem.getCarbsPer100g());
                pstmt.setInt(8, foodItem.isComposite() ? 1 : 0);
                pstmt.setObject(9, foodItem.getFoodCategory() != null ? foodItem.getFoodCategory().getId() : null);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    LOGGER.error("Создание food item {} не удалось, 0 затронутых строк.", foodItem.getName());
                    throw new DataAccessException("Создание food item " + foodItem.getName() + " не удалось, 0 затронутых строк.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        foodItem.setId(generatedKeys.getLong(1));
                        LOGGER.info("FoodItem {} сохранен с ID {}", foodItem.getName(), foodItem.getId());
                    } else {
                        LOGGER.error("Сохранение FoodItem не удалось, ID не было получено для {}", foodItem.getName());
                        throw new DataAccessException("Сохранение FoodItem не удалось, ID не было получено.");
                    }
                }
            }

            if (foodItem.isComposite() && foodItem.getComponents() != null && !foodItem.getComponents().isEmpty()) {
                for (FoodComponent component : foodItem.getComponents()) {
                    if (component.getIngredientFoodItemId() == 0) {
                        LOGGER.error("Обнаружен FoodComponent c ID 0 при сохранении food item {}", foodItem.getName());
                        throw new DataAccessException("Создание food component при сохранении food item" + foodItem.getName() + " не удалось, обнаружен компонент с ID 0");
                    }
                    try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_FOOD_COMPONENT, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setLong(1, foodItem.getId());
                        pstmt.setLong(2, component.getIngredientFoodItemId());
                        pstmt.setDouble(3, component.getAmountInGrams());

                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows == 0) {
                            LOGGER.error("Создание food component c id {} при сохранении food item {} не удалось, 0 затронутых строк.", component.getIngredientFoodItemId() ,foodItem.getName());
                            throw new DataAccessException("Создание food component для food item " + foodItem.getName() + " не удалось, 0 затронутых строк.");
                        }
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                component.setId(generatedKeys.getLong(1));
                                LOGGER.debug("FoodComponent {} при сохранении food item {} сохранен с ID {}",
                                        component.getIngredientFoodItemId(), foodItem.getName(), component.getId());
                            } else {
                                LOGGER.error("Создание food component при сохранении food item {} не удалось, ID не было получено для {}",
                                        foodItem.getName(), component.getIngredientFoodItemId());
                                throw new DataAccessException("Сохранение FoodComponent не удалось, ID не было получено.");
                            }
                        }
                    }
                }
                LOGGER.info("Сохранено {} компонентов при сохранении food item {}", foodItem.getComponents().size(), foodItem.getName());
            }
            connection.commit();
            return foodItem;
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при сохранении FoodItem {}. SQLState: {}, ErrorCode: {}, message: {}",
                    foodItem.getName(), e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.warn("Откат транзакции после неудачного сохранения FoodItem {}", foodItem.getName());
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Произошло SQLException при откате транзакции после неудачного сохранения FoodItem {}. SQLState: {}, ErrorCode: {}, message: {}",
                            foodItem.getName(), rollbackEx.getSQLState(), rollbackEx.getErrorCode(), rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw SqlExceptionTranslator.translate(e, "сохранении FoodItem " + foodItem.getName());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Произошло SQLException при попытке закрытия соединения после сохранения FoodItem {}. SQLState: {}, ErrorCode: {}, message: {}",
                           foodItem.getName(), closeEx.getSQLState(), closeEx.getErrorCode(), closeEx.getMessage(), closeEx);
                }
            }
        }
    }

    public Optional<FoodItem> findById(long id) {
        FoodItem foodItem = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_FOOD_ITEM_BY_ID)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    FoodCategory foodCategory = null;
                    if (rs.getObject("category_id") != null) {
                        foodCategory = new FoodCategory(rs.getLong("category_id"), rs.getString("category_name"));
                    }
                    foodItem = new FoodItem.Builder()
                            .setId(rs.getLong("id"))
                            .setName(rs.getString("name"))
                            .setCaloriesPer100g(rs.getDouble("calories_per_100g"))
                            .setServingSizeInGrams(rs.getDouble("serving_size_in_grams"))
                            .setUnit(Unit.valueOf(rs.getString("unit")))
                            .setProteinsPer100g(rs.getDouble("proteins_per_100g"))
                            .setFatsPer100g(rs.getDouble("fats_per_100g"))
                            .setCarbsPer100g(rs.getDouble("carbs_per_100g"))
                            .setComposite(rs.getInt("is_composite") == 1)
                            .setFoodCategory(foodCategory)
                            .setComponents(null)
                            .build();

                    if (foodItem.isComposite()) {
                        List<FoodComponent> components = new ArrayList<>();
                        try (PreparedStatement pstmtComponents = connection.prepareStatement(SqlQueries.SELECT_FOOD_COMPONENT)) {
                            pstmtComponents.setLong(1, foodItem.getId());
                            try (ResultSet rsComponents = pstmtComponents.executeQuery()) {
                                while (rsComponents.next()) {
                                    components.add(new FoodComponent(
                                            rsComponents.getLong("id"),
                                            rsComponents.getLong("ingredient_food_item_id"),
                                            rsComponents.getDouble("amount_in_grams")
                                    ));
                                }
                            }
                        }
                        foodItem.setComponents(components.isEmpty() ? null : components);
                    }
                    LOGGER.debug("Найден food item {} по ID {}", foodItem.getName(), id);
                } else {
                    LOGGER.debug("food item c ID {} не найден.", id);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при поиске FoodItem c ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                    id, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "поиске FoodItem c ID " + id);
        }
        return Optional.ofNullable(foodItem);
    }

    public Optional<FoodItem> findByName(String name) {
        FoodItem foodItem = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_FOOD_ITEM_BY_NAME)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    FoodCategory foodCategory = null;
                    if (rs.getObject("category_id") != null) {
                        foodCategory = new FoodCategory(rs.getLong("category_id"), rs.getString("category_name"));
                    }
                    foodItem = new FoodItem.Builder()
                            .setId(rs.getLong("id"))
                            .setName(rs.getString("name"))
                            .setCaloriesPer100g(rs.getDouble("calories_per_100g"))
                            .setServingSizeInGrams(rs.getDouble("serving_size_in_grams"))
                            .setUnit(Unit.valueOf(rs.getString("unit")))
                            .setProteinsPer100g(rs.getDouble("proteins_per_100g"))
                            .setFatsPer100g(rs.getDouble("fats_per_100g"))
                            .setCarbsPer100g(rs.getDouble("carbs_per_100g"))
                            .setComposite(rs.getInt("is_composite") == 1)
                            .setFoodCategory(foodCategory)
                            .setComponents(null)
                            .build();

                    if (foodItem.isComposite()) {
                        List<FoodComponent> components = new ArrayList<>();
                        try (PreparedStatement pstmtComponents = connection.prepareStatement(SqlQueries.SELECT_FOOD_COMPONENT)) {
                            pstmtComponents.setLong(1, foodItem.getId());
                            try (ResultSet rsComponents = pstmtComponents.executeQuery()) {
                                while (rsComponents.next()) {
                                    components.add(new FoodComponent(
                                            rsComponents.getLong("id"),
                                            rsComponents.getLong("ingredient_food_item_id"),
                                            rsComponents.getDouble("amount_in_grams")
                                    ));
                                }
                            }
                        }
                        foodItem.setComponents(components.isEmpty() ? null : components);
                    }
                    LOGGER.debug("Найден food item c ID {} по имени {}", foodItem.getId(), name);
                } else {
                    LOGGER.debug("food item c именем {} не найден.", name);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при поиске FoodItem по имени {}. SQLState: {}, ErrorCode: {}, message: {}",
                    name, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "поиске FoodItem по имени " + name);
        }
        return Optional.ofNullable(foodItem);
    }

    public boolean update(FoodItem foodItem) {
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_FOOD_ITEM)) {
                pstmt.setString(1, foodItem.getName());
                pstmt.setDouble(2, foodItem.getCaloriesPer100g());
                pstmt.setDouble(3, foodItem.getServingSizeInGrams());
                pstmt.setString(4, foodItem.getUnit().name());
                pstmt.setDouble(5, foodItem.getProteinsPer100g());
                pstmt.setDouble(6, foodItem.getFatsPer100g());
                pstmt.setDouble(7, foodItem.getCarbsPer100g());
                pstmt.setInt(8, foodItem.isComposite() ? 1 : 0);
                pstmt.setObject(9, foodItem.getFoodCategory() != null ? foodItem.getFoodCategory().getId() : null);
                pstmt.setLong(10, foodItem.getId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    LOGGER.warn("food item c ID {} для обновления не найден.", foodItem.getId());
                    return false;
                }
                LOGGER.info("food item {} c ID {} обновлен.", foodItem.getName(), foodItem.getId());
            }

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_FOOD_COMPONENT)) {
                pstmt.setLong(1, foodItem.getId());
                int deletedComponents = pstmt.executeUpdate();
                if (deletedComponents > 0) {
                    LOGGER.info("Удалено {} существующих компонентов для food item c ID {}", deletedComponents, foodItem.getId());
                } else {
                    LOGGER.debug("Для food item с ID {} не найдено существующих компонентов для удаления.", foodItem.getId());
                }
            }

            if (foodItem.isComposite() && foodItem.getComponents() != null && !foodItem.getComponents().isEmpty()) {
                for (FoodComponent component : foodItem.getComponents()) {
                    if (component.getIngredientFoodItemId() == 0) {
                        LOGGER.error("Обнаружен FoodComponent c ID 0 при обновлении food item {}", foodItem.getName());
                        throw new DataAccessException("Создание food component при сохранении food item" + foodItem.getName() + " не удалось, обнаружен компонент с ID 0");
                    }
                    try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_FOOD_COMPONENT, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setLong(1, foodItem.getId());
                        pstmt.setLong(2, component.getIngredientFoodItemId());
                        pstmt.setDouble(3, component.getAmountInGrams());

                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows == 0) {
                            LOGGER.error("Создание food component {} при обновлении food item {} не удалось, 0 затронутых строк.",component.getIngredientFoodItemId(), foodItem.getName());
                            throw new DataAccessException("Создание food component для food item " + foodItem.getName() + " не удалось, 0 затронутых строк.");
                        }
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                component.setId(generatedKeys.getLong(1));
                                LOGGER.debug("FoodComponent {} при обновлении food item {} сохранен с ID {}", component.getIngredientFoodItemId(), foodItem.getName() ,component.getId());
                            } else {
                                LOGGER.error("Создание FoodComponent при обновлении food item {} не удалось, ID не было получено для {}",
                                        foodItem.getName(),component.getIngredientFoodItemId());
                                throw new DataAccessException("Создание FoodComponent не удалось, ID не было получено.");
                            }
                        }
                    }
                }
                LOGGER.info("Сохранено {} компонентов при обновлении food item {}", foodItem.getComponents().size(), foodItem.getName());
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при обновлении FoodItem . SQLState: {}, ErrorCode: {}, message: {}",
                    foodItem.getName(), e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.warn("Откат транзакции после неудачного обновления FoodItem {}", foodItem.getName());
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Произошло SQLException при откате транзакции после неудачного обновления FoodItem {}. SQLState: {}, ErrorCode: {}, message: {}",
                            foodItem.getName(), rollbackEx.getSQLState(), rollbackEx.getErrorCode(), rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw SqlExceptionTranslator.translate(e, "обновлении FoodItem " + foodItem.getName());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Произошло SQLException при попытке закрытия соединения после сохранения FoodItem {}. SQLState: {}, ErrorCode: {}, message: {}",
                            foodItem.getName(), closeEx.getSQLState(), closeEx.getErrorCode(), closeEx.getMessage(), closeEx);
                }
            }
        }
    }

    public boolean delete(long id) {
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_FOOD_ITEM)) {
                pstmt.setLong(1, id);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    LOGGER.warn("food item c ID {} для удаления не найден.", id);
                    return false;
                } else {
                    LOGGER.info("food item c ID {} успешно удален.", id);
                    connection.commit();
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при удалении FoodItem с ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                    id, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.warn("Откат транзакции после неудачного удаления FoodItem с ID {}", id);
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Произошло SQLException при откате транзакции после неудачного удаления FoodItem с ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                            id, rollbackEx.getSQLState(), rollbackEx.getErrorCode(), rollbackEx.getMessage(), e);
                }
            }
            throw SqlExceptionTranslator.translate(e, "удалении FoodItem с ID " + id);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Произошло SQLException при попытке закрытия соединения после удаления FoodItem c ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                            id, closeEx.getSQLState(), closeEx.getErrorCode(), closeEx.getMessage(), closeEx);
                }
            }
        }
    }

    public List<FoodItem> findAll(boolean loadComponents) {
        List<FoodItem> foodItems = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_ALL_FOOD_ITEMS);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                FoodCategory foodCategory = null;
                if (rs.getObject("category_id") != null) {
                    foodCategory = new FoodCategory(rs.getLong("category_id"), rs.getString("category_name"));
                }

                FoodItem foodItem = new FoodItem.Builder()
                        .setId(rs.getLong("id"))
                        .setName(rs.getString("name"))
                        .setCaloriesPer100g(rs.getDouble("calories_per_100g"))
                        .setServingSizeInGrams(rs.getDouble("serving_size_in_grams"))
                        .setUnit(Unit.valueOf(rs.getString("unit")))
                        .setProteinsPer100g(rs.getDouble("proteins_per_100g"))
                        .setFatsPer100g(rs.getDouble("fats_per_100g"))
                        .setCarbsPer100g(rs.getDouble("carbs_per_100g"))
                        .setComposite(rs.getInt("is_composite") == 1)
                        .setFoodCategory(foodCategory)
                        .setComponents(null)
                        .build();

                if (loadComponents && foodItem.isComposite()) {
                    List<FoodComponent> components = new ArrayList<>();
                    try (PreparedStatement pstmtComponents = connection.prepareStatement(SqlQueries.SELECT_FOOD_COMPONENT)) {
                        pstmtComponents.setLong(1, foodItem.getId());
                        try (ResultSet rsComponents = pstmtComponents.executeQuery()) {
                            while (rsComponents.next()) {
                                components.add(new FoodComponent(
                                        rsComponents.getLong("id"),
                                        rsComponents.getLong("ingredient_food_item_id"),
                                        rsComponents.getDouble("amount_in_grams")
                                ));
                            }
                        }
                    }
                    foodItem.setComponents(components.isEmpty() ? null : components);
                }
                foodItems.add(foodItem);
            }
            LOGGER.debug("Получено {} food items из БД.", foodItems.size());
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при получении всех FoodItem из БД. SQLState: {}, ErrorCode: {}, message: {}",
                   e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "получении всех FoodItem из БД.");
        }
        return foodItems;
    }
}