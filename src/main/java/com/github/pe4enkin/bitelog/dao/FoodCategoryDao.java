package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.FoodCategory;
import com.github.pe4enkin.bitelog.sql.SqlQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodCategoryDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(FoodCategoryDao.class);
    private final DataSource dataSource;

    public FoodCategoryDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createTables() throws SQLException {
        try (Connection connection = DataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute(SqlQueries.CREATE_FOOD_CATEGORIES_TABLE);
            LOGGER.info("Таблица food_categories успешно создана (или уже существовала)");
        } catch (SQLException e) {
            LOGGER.error("Ошибка создания таблиц food_categories: ", e);
            throw e;
        }
    }

    public FoodCategory save(FoodCategory foodCategory) throws SQLException {
        Connection connection = null;
        try {
            connection = DataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_FOOD_CATEGORY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, foodCategory.getName());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Создание food category " + foodCategory.getName() + " не удалось.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        foodCategory.setId(generatedKeys.getLong(1));
                        LOGGER.info("FoodCategory {} сохранен {}", foodCategory.getName(), foodCategory.getId());
                    } else {
                        throw new SQLException("Создание food category не удалось, ID не было получено.");
                    }
                }
            }
            connection.commit();
            return foodCategory;
        } catch (SQLException e) {
            LOGGER.error("Ошибка сохранения FoodCategory {}. Откат транзакции.", foodCategory.getName(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Ошибка отката транзакции для сохранения FoodCategory {}", foodCategory.getName(), rollbackEx);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Ошибка закрытия соединения после сохранения food category", closeEx);
                }
            }
        }
    }

    public Optional<FoodCategory> findById(long id) throws SQLException {
        FoodCategory foodCategory = null;

        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_FOOD_CATEGORY)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foodCategory = new FoodCategory(
                            rs.getLong("id"),
                            rs.getString("name")
                    );
                    LOGGER.info("Найден food category {}", foodCategory.getName());
                } else {
                    LOGGER.info("food category c ID {} не найден.", id);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Ошибка при поиске FoodCategory с ID {}", id, e);
            throw e;
        }
        return Optional.ofNullable(foodCategory);
    }

    public boolean update(FoodCategory foodCategory) throws SQLException {
        Connection connection = null;

        try {
            connection = DatabaseConnectionManager.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_FOOD_CATEGORY)) {
                pstmt.setString(1, foodCategory.getName());
                pstmt.setLong(2, foodCategory.getId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    LOGGER.warn("food category c ID {} для обновления не найден.", foodCategory.getId());
                    return false;
                }
                LOGGER.info("food category {} c ID {} обновлен.", foodCategory.getName(), foodCategory.getId());
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Ошибка обновления FoodCategory {}. Откат транзакции.", foodCategory.getName(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Ошибка отката транзакции для обновления FoodCategory {}", foodCategory.getName(), rollbackEx);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Ошибка закрытия соединения после обновления food category", closeEx);
                }
            }
        }
    }

    public boolean delete(long id) throws SQLException {
        Connection connection = null;

        try {
            connection = DatabaseConnectionManager.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_FOOD_CATEGORY)) {
                pstmt.setLong(1, id);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    LOGGER.info("food category c ID {} успешно удален.", id);
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    LOGGER.warn("food category c ID {} для удаления не найден.", id);
                    return false;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Ошибка удаления FoodCategory с ID {}. Откат транзакции.", id, e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Ошибка отката транзакции для удаления FoodCategory с ID {}", id, rollbackEx);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Ошибка закрытия соединения после удаления food category.", closeEx);
                }
            }
        }
    }

    public List<FoodCategory> findAll() throws SQLException {
        List<FoodCategory> foodCategories = new ArrayList<>();

        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_ALL_FOOD_CATEGORY);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                FoodCategory foodCategory = new FoodCategory(
                        rs.getLong("id"),
                        rs.getString("name")
                );
                foodCategories.add(foodCategory);
            }
            LOGGER.info("Получено {} food category из БД.", foodCategories.size());
        } catch (SQLException e) {
            LOGGER.error("Ошибка получения всех food category из БД.", e);
            throw e;
        }
        return foodCategories;
    }
}