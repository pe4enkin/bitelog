package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.FoodComponent;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.sql.SqlQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

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
}