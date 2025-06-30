package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import com.github.pe4enkin.bitelog.model.FoodComponent;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.model.Unit;
import com.github.pe4enkin.bitelog.sql.SqlQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDao {

    private static final Logger logger = LoggerFactory.getLogger(FoodItemDao.class);

    public FoodItemDao() {
    }

    public void createTables() throws SQLException {

        try (Connection connection = DatabaseConnectionManager.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute(SqlQueries.CREATE_FOOD_ITEMS_TABLE);
            stmt.execute(SqlQueries.CREATE_FOOD_COMPONENTS_TABLE);
            logger.info("Таблицы food_items и food_components успешно созданы (или уже существовали)");
        } catch (SQLException e) {
            logger.error("Ошибка создания таблиц food_items и food_components: ", e);
            throw e;
        }
    }

    public FoodItem save(FoodItem foodItem) throws SQLException {

        Connection connection = null;
        try {
            connection = DatabaseConnectionManager.getConnection();
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
                    throw new SQLException("Создание food item не удалось, ни одна строчка не была затронута.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        foodItem.setId(generatedKeys.getLong(1));
                        logger.info("FoodItem '{}' saved with ID: {}", foodItem.getName(), foodItem.getId());
                    } else {
                        throw new SQLException("Создание food item не удалось, ID не было получено.");
                    }
                }
            }

            if (foodItem.isComposite() && foodItem.getComponents() != null && !foodItem.getComponents().isEmpty()) {
                try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_FOOD_COMPONENT)) {
                    for (FoodComponent component : foodItem.getComponents()) {
                        if (component.getIngredientFoodItemId() == 0) {
                            logger.warn("Обнаружен FoodComponent c ID 0.");
                        }
                        pstmt.setLong(1, foodItem.getId());
                        pstmt.setLong(2, component.getIngredientFoodItemId());
                        pstmt.setDouble(3, component.getAmountInGrams());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    logger.info("Сохранено {} компонентов для food item с ID: {}", foodItem.getComponents().size(), foodItem.getId());
                }
            }
            connection.commit();
            return foodItem;
        } catch (SQLException e) {
            logger.error("Ошибка сохранения food item: {}. Откат транзакции.", foodItem.getName(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Ошибка отката транзакции для food item: {}", foodItem.getName(), rollbackEx);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    logger.error("Ошибка закрытия соединения после сохранения food item", closeEx);
                }
            }
        }
    }

    public FoodItem findById(long id) throws SQLException {

        FoodItem foodItem = null;
        Connection connection = null;

        try {
            connection = DatabaseConnectionManager.getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_FOOD_ITEM)) {
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
                                                rsComponents.getLong("parent_food_item_id"),
                                                rsComponents.getLong("ingredient_food_item_id"),
                                                rsComponents.getDouble("amount_in_grams")
                                        ));
                                    }
                                }
                            }
                            foodItem.setComponents(components.isEmpty() ? null : components);
                        }
                        logger.info("Найден FoodItem: {}", foodItem.getName());
                    } else {
                        logger.info("FoodItem c ID {} не найден.", id);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка SQL при поиске FoodItem с ID: {}", id, e);
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    logger.error("Ошибка закрытия соединения после поиска food item", closeEx);
                }
            }
        }
        return foodItem;
    }

}