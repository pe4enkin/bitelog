package com.github.pe4enkin.bitelog.dao;

import com.github.pe4enkin.bitelog.dao.util.SqlExceptionTranslator;
import com.github.pe4enkin.bitelog.sql.SqlQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
}
