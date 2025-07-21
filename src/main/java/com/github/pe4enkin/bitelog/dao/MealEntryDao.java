package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.dao.exception.DataAccessException;
import com.github.pe4enkin.bitelog.dao.util.SqlExceptionTranslator;
import com.github.pe4enkin.bitelog.model.MealCategory;
import com.github.pe4enkin.bitelog.model.MealComponent;
import com.github.pe4enkin.bitelog.model.MealEntry;
import com.github.pe4enkin.bitelog.sql.SqlQueries;
import com.github.pe4enkin.bitelog.util.DateTimeFormatterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MealEntryDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MealEntryDao.class);
    private final DataSource dataSource;

    public MealEntryDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createTables() {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute(SqlQueries.CREATE_MEAL_ENTRIES_TABLE);
            stmt.execute(SqlQueries.CREATE_MEAL_COMPONENTS_TABLE);
            LOGGER.info("Таблицы meal_entries и meal_components успешно созданы (или уже существовали).");
        } catch (SQLException e) {
            LOGGER.error("Ошибка при создании таблиц meal_entries и meal_components. SQLState: {}, ErrorCode: {}, message: {}",
                    e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "создании таблиц meal_entries и meal_components");
        }
    }

    public MealEntry save(MealEntry mealEntry) {
        Connection connection = null;
        String logMealDateTime = DateTimeFormatterUtil.formatDateTime(mealEntry.getDate(), mealEntry.getTime());
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_MEAL_ENTRY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDate(1, Date.valueOf(mealEntry.getDate()));
                pstmt.setTime(2, Time.valueOf(mealEntry.getTime()));
                pstmt.setString(3, mealEntry.getMealCategory().name());
                pstmt.setString(4, mealEntry.getNotes());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    LOGGER.error("Создание meal entry от {} не удалось, 0 затронутых строк.", logMealDateTime);
                    throw new DataAccessException("Создание meal entry от " + logMealDateTime + " не удалось, 0 затронутых строк.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        mealEntry.setId(generatedKeys.getLong(1));
                        LOGGER.info("MealEntry от {} сохранен с ID {}", logMealDateTime, mealEntry.getId());
                    } else {
                        LOGGER.error("Сохранение MealEntry не удалось, ID не было получено для {}", logMealDateTime);
                        throw new DataAccessException("Сохранение MealEntry не удалось, ID не было получено.");
                    }
                }
            }

            if (!mealEntry.getComponents().isEmpty()) {
                for (MealComponent component : mealEntry.getComponents()) {
                    if (component.getFoodItemId() == 0) {
                        LOGGER.error("Обнаружен MealComponent c ID продукта 0 при сохранении meal entry от {}", logMealDateTime);
                        throw new DataAccessException("Создание meal component при сохранении meal entry от " + logMealDateTime + " не удалось, обнаружен компонент с ID продукта 0");
                    }
                    try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_MEAL_COMPONENT, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setLong(1, mealEntry.getId());
                        pstmt.setLong(2, component.getFoodItemId());
                        pstmt.setDouble(3, component.getAmountInGrams());

                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows == 0) {
                            LOGGER.error("Создание meal component c id продукта {} при сохранении meal entry от {} не удалось, 0 затронутых строк.", component.getFoodItemId(), logMealDateTime);
                            throw new DataAccessException("Создание meal component для meal entry от " + logMealDateTime + " не удалось, 0 затронутых строк.");
                        }
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                component.setId(generatedKeys.getLong(1));
                                LOGGER.debug("MealComponent с ID продукта {} при сохранении meal entry от {} сохранен с ID {}",
                                        component.getFoodItemId(), logMealDateTime, component.getId());
                            } else {
                                LOGGER.error("Создание MealComponent при сохранении meal entry от {} не удалось, ID не было получено для компонента с ID продукта {}",
                                        logMealDateTime, component.getFoodItemId());
                                throw new DataAccessException("Создание MealComponent не удалось, ID не было получено.");
                            }
                        }
                    }
                }
                LOGGER.info("Сохранено {} компонентов при сохранении meal entry от {}", mealEntry.getComponents().size(), logMealDateTime);
            }
            connection.commit();
            return mealEntry;
        } catch (SQLException e) {
            LOGGER.error("Ошибка при сохранении MealEntry от {}. SQLState: {}, ErrorCode: {}, message: {}",
                    logMealDateTime, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.warn("Откат транзакции после неудачного сохранения MealEntry от {}", logMealDateTime);
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Ошибка при откате транзакции после неудачного сохранения MealEntry от {}. SQLState: {}, ErrorCode: {}, message: {}",
                            logMealDateTime, rollbackEx.getSQLState(), rollbackEx.getErrorCode(), rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw SqlExceptionTranslator.translate(e, "сохранении MealEntry от " + logMealDateTime);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Ошибка при попытке закрытия соединения после сохранения MealEntry от {}. SQLState: {}, ErrorCode: {}, message: {}",
                            logMealDateTime, closeEx.getSQLState(), closeEx.getErrorCode(), closeEx.getMessage(), closeEx);
                }
            }
        }
    }

    public Optional<MealEntry> findById(long id) {
        MealEntry mealEntry = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_MEAL_ENTRY_BY_ID)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date date = rs.getDate("date");
                    Time time = rs.getTime("time");
                    mealEntry = new MealEntry.Builder()
                            .setId(rs.getLong("id"))
                            .setDate(date != null ? date.toLocalDate() : null)
                            .setTime(time != null ? time.toLocalTime() : null)
                            .setMealCategory(MealCategory.valueOf(rs.getString("meal_category")))
                            .setNotes(rs.getString("notes"))
                            .build();

                    List<MealComponent> components = new ArrayList<>();
                    try (PreparedStatement pstmtComponents = connection.prepareStatement(SqlQueries.SELECT_MEAL_COMPONENT)) {
                        pstmtComponents.setLong(1, mealEntry.getId());
                        try (ResultSet rsComponents = pstmtComponents.executeQuery()) {
                            while (rsComponents.next()) {
                                components.add(new MealComponent(
                                        rsComponents.getLong("id"),
                                        rsComponents.getLong("food_item_id"),
                                        rsComponents.getDouble("amount_in_grams")
                                ));
                            }
                        }
                    }
                    mealEntry.setComponents(components);
                    LOGGER.debug("Найден meal entry от {} по ID {}", DateTimeFormatterUtil.formatDateTime(mealEntry.getDate(), mealEntry.getTime()), id);
                } else {
                    LOGGER.debug("meal entry c ID {} не найден.", id);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Ошибка при поиске MealEntry c ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                    id, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "поиске MealEntry c ID " + id);
        }
        return Optional.ofNullable(mealEntry);
    }

    public boolean update(MealEntry mealEntry) {
        Connection connection = null;
        String logMealDateTime = DateTimeFormatterUtil.formatDateTime(mealEntry.getDate(), mealEntry.getTime());

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_MEAL_ENTRY)) {
                pstmt.setDate(1, Date.valueOf(mealEntry.getDate()));
                pstmt.setTime(2, Time.valueOf(mealEntry.getTime()));
                pstmt.setString(3, mealEntry.getMealCategory().name());
                pstmt.setString(4, mealEntry.getNotes());
                pstmt.setLong(5, mealEntry.getId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    LOGGER.warn("meal entry c ID {} для обновления не найден.", mealEntry.getId());
                    return false;
                }
                LOGGER.info("meal entry от {} c ID {} обновлен.", logMealDateTime, mealEntry.getId());
            }

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_MEAL_COMPONENT)) {
                pstmt.setLong(1, mealEntry.getId());
                int deletedComponents = pstmt.executeUpdate();
                if (deletedComponents > 0) {
                    LOGGER.info("Удалено {} существующих компонентов для meal entry c ID {}", deletedComponents, mealEntry.getId());
                } else {
                    LOGGER.debug("Для meal entry с ID {} не найдено существующих компонентов для удаления.", mealEntry.getId());
                }
            }

            if (!mealEntry.getComponents().isEmpty()) {
                for (MealComponent component : mealEntry.getComponents()) {
                    if (component.getFoodItemId() == 0) {
                        LOGGER.error("Обнаружен MealComponent c ID продукта 0 при обновлении meal entry от {}", logMealDateTime);
                        throw new DataAccessException("Создание meal component при обновлении meal entry от " + logMealDateTime + " не удалось, обнаружен компонент с ID продукта 0");
                    }
                    try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_MEAL_COMPONENT, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setLong(1, mealEntry.getId());
                        pstmt.setLong(2, component.getFoodItemId());
                        pstmt.setDouble(3, component.getAmountInGrams());

                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows == 0) {
                            LOGGER.error("Создание meal component c id продукта {} при обновлении meal entry от {} не удалось, 0 затронутых строк.", component.getFoodItemId(), logMealDateTime);
                            throw new DataAccessException("Создание meal component для meal entry от " + logMealDateTime + " не удалось, 0 затронутых строк.");
                        }
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                component.setId(generatedKeys.getLong(1));
                                LOGGER.debug("MealComponent с ID продукта {} при обновлении meal entry от {} сохранен с ID {}",
                                        component.getFoodItemId(), logMealDateTime, component.getId());
                            } else {
                                LOGGER.error("Создание MealComponent при обновлении meal entry от {} не удалось, ID не было получено для компонента с ID продукта {}",
                                        logMealDateTime, component.getFoodItemId());
                                throw new DataAccessException("Создание MealComponent не удалось, ID не было получено.");
                            }
                        }
                    }
                }
                LOGGER.info("Сохранено {} компонентов при обновлении meal entry от {}", mealEntry.getComponents().size(), logMealDateTime);
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Ошибка при обновлении MealEntry от {}. SQLState: {}, ErrorCode: {}, message: {}",
                    logMealDateTime, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.warn("Откат транзакции после неудачного обновления MealEntry от {}", logMealDateTime);
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Ошибка при откате транзакции после неудачного обновления MealEntry от {}. SQLState: {}, ErrorCode: {}, message: {}",
                            logMealDateTime, rollbackEx.getSQLState(), rollbackEx.getErrorCode(), rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw SqlExceptionTranslator.translate(e, "обновлении MealEntry от " + logMealDateTime);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Ошибка при попытке закрытия соединения после обновления MealEntry от {}. SQLState: {}, ErrorCode: {}, message: {}",
                            logMealDateTime, closeEx.getSQLState(), closeEx.getErrorCode(), closeEx.getMessage(), closeEx);
                }
            }
        }
    }

    public boolean delete(long id) {
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_MEAL_ENTRY)) {
                pstmt.setLong(1, id);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    LOGGER.warn("meal entry с ID {} для удаления не найжден.", id);
                    return false;
                } else {
                    LOGGER.info("meal entry c ID {} успешно удален.", id);
                    connection.commit();
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Ошибка при удалении MealEntry с ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                    id, e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.warn("Откат транзакции после неудачного удаления MealEntry с ID {}", id);
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Ошибка при откате транзакции после неудачного удаления MealEntry с ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                            id, rollbackEx.getSQLState(), rollbackEx.getErrorCode(), rollbackEx.getMessage(), e);
                }
            }
            throw SqlExceptionTranslator.translate(e, "удалении MealEntry с ID " + id);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Ошибка при попытке закрытия соединения после удаления MealEntry c ID {}. SQLState: {}, ErrorCode: {}, message: {}",
                            id, closeEx.getSQLState(), closeEx.getErrorCode(), closeEx.getMessage(), closeEx);
                }
            }
        }
    }

    public List<MealEntry> findAllByDate(LocalDate searchDate) {
        List<MealEntry> mealEntries = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SELECT_ALL_MEAL_ENTRIES_BY_DATE)) {
            pstmt.setDate(1, Date.valueOf(searchDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Date date = rs.getDate("date");
                    Time time = rs.getTime("time");
                    MealEntry mealEntry = new MealEntry.Builder()
                            .setId(rs.getLong("id"))
                            .setDate(date != null ? date.toLocalDate() : null)
                            .setTime(time != null ? time.toLocalTime() : null)
                            .setMealCategory(MealCategory.valueOf(rs.getString("meal_category")))
                            .setNotes(rs.getString("notes"))
                            .build();
                    List<MealComponent> components = new ArrayList<>();
                    try (PreparedStatement pstmtComponents = connection.prepareStatement(SqlQueries.SELECT_MEAL_COMPONENT)) {
                        pstmtComponents.setLong(1, mealEntry.getId());
                        try (ResultSet rsComponents = pstmtComponents.executeQuery()) {
                            while (rsComponents.next()) {
                                components.add(new MealComponent(
                                        rsComponents.getLong("id"),
                                        rsComponents.getLong("food_item_id"),
                                        rsComponents.getDouble("amount_in_grams")
                                ));
                            }
                        }
                    }
                    mealEntry.setComponents(components);
                    mealEntries.add(mealEntry);
                }
                LOGGER.debug("Получено {} meal entries из БД на дату {}.", mealEntries.size(), DateTimeFormatterUtil.formatDateWithDots(searchDate));
            }
        } catch (SQLException e) {
            LOGGER.error("Ошибка при получении MealEntries на дату {} из БД. SQLState: {}, ErrorCode: {}, message: {}",
                    DateTimeFormatterUtil.formatDateWithDots(searchDate), e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw SqlExceptionTranslator.translate(e, "получении MealEntries на дату из БД.");
        }
        return mealEntries;
    }
}