package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.dao.exception.ConstraintViolationException;
import com.github.pe4enkin.bitelog.dao.exception.DataAccessException;
import com.github.pe4enkin.bitelog.dao.exception.DuplicateKeyException;
import com.github.pe4enkin.bitelog.dao.exception.ForeignKeyViolationException;
import com.github.pe4enkin.bitelog.dao.util.SqlExceptionTranslator;
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

    public void createTables() {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute(SqlQueries.CREATE_FOOD_CATEGORIES_TABLE);
            LOGGER.info("Таблица food_categories успешно создана (или уже существовала)");
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при создании таблицы food_categories. SQLState: {}, ErrorCode: {}, message: {}",
                   e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "создании таблицы food_categories");
        }
    }

    public FoodCategory save(FoodCategory foodCategory) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_FOOD_CATEGORY, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, foodCategory.getName());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.error("Создание food category {} не удалось, 0 затронутых строк.", foodCategory.getName());
                throw new DataAccessException("Создание food category " + foodCategory.getName() + " не удалось, 0 затронутых строк.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    foodCategory.setId(generatedKeys.getLong(1));
                    LOGGER.info("FoodCategory {} сохранен c ID {}", foodCategory.getName(), foodCategory.getId());
                } else {
                    LOGGER.error("Сохранение FoodCategory не удалось, ID не было получено для {}", foodCategory.getName());
                    throw new DataAccessException("Сохранение FoodCategory не удалось, ID не было получено.");
                }
            }
            return foodCategory;
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при сохранении FoodCategory {}. SQLState: {}, ErrorCode: {}, message: {}",
                    foodCategory.getName(), e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "сохранении FoodCategory " + foodCategory.getName());
        }
    }

    public Optional<FoodCategory> findById(long id) {
        FoodCategory foodCategory = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_FOOD_CATEGORY_BY_ID)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foodCategory = new FoodCategory(
                            rs.getLong("id"),
                            rs.getString("name")
                    );
                    LOGGER.debug("Найден food category {}", foodCategory.getName());
                } else {
                    LOGGER.debug("food category c ID {} не найден.", id);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при поиске FoodCategory c ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                    id, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "поиске FoodCategory c ID " + id);
        }
        return Optional.ofNullable(foodCategory);
    }

    public Optional<FoodCategory> findByName(String name) {
        FoodCategory foodCategory = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_FOOD_CATEGORY_BY_NAME)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foodCategory = new FoodCategory(
                            rs.getLong("id"),
                            rs.getString("name")
                    );
                    LOGGER.debug("Найден food category {}", foodCategory.getName());
                } else {
                    LOGGER.debug("food category c именем {} не найден.", name);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при поиске FoodCategory по имени {}. SQLState: {}, ErrorCode: {}, message: {}",
                    name, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "поиске FoodCategory по имени " + name);
        }
        return Optional.ofNullable(foodCategory);
    }

    public boolean update(FoodCategory foodCategory) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_FOOD_CATEGORY)) {
            pstmt.setString(1, foodCategory.getName());
            pstmt.setLong(2, foodCategory.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.warn("food category c ID {} для обновления не найден.", foodCategory.getId());
                return false;
            }
            LOGGER.info("food category {} c ID {} обновлен.", foodCategory.getName(), foodCategory.getId());
            return true;
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при обновлении FoodCategory {}. SQLState: {}, ErrorCode: {}, message: {}",
                    foodCategory.getName(), e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "обновлении FoodCategory " + foodCategory.getName());
        }
    }

    public boolean delete(long id) {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_FOOD_CATEGORY)) {
            pstmt.setLong(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.warn("food category c ID {} для удаления не найден.", id);
                return false;
            } else {
                LOGGER.info("food category c ID {} успешно удален.", id);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при удалении FoodCategory c ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                    id, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "удалении FoodCategory c ID " + id);
        }
    }

    public List<FoodCategory> findAll() {
        List<FoodCategory> foodCategories = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_ALL_FOOD_CATEGORY);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                FoodCategory foodCategory = new FoodCategory(
                        rs.getLong("id"),
                        rs.getString("name")
                );
                foodCategories.add(foodCategory);
            }
            LOGGER.debug("Получено {} food category из БД.", foodCategories.size());
        } catch (SQLException e) {
            LOGGER.error("Произошло SQLException при получении всех FoodCategory из БД. SQLState: {}, ErrorCode: {}, message: {}",
                    e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "получении всех FoodCategory из БД.");
        }
        return foodCategories;
    }
}