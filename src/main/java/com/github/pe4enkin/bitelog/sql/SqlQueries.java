package com.github.pe4enkin.bitelog.sql;

public class SqlQueries {
    public static final String CREATE_FOOD_ITEMS_TABLE = """
            CREATE TABLE IF NOT EXISTS food_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                calories_per_100g REAL NOT NULL,
                serving_size_in_grams REAL NOT NULL,
                unit TEXT NOT NULL,
                proteins_per_100g REAL NOT NULL,
                fats_per_100g REAL NOT NULL,
                carbs_per_100g REAL NOT NULL,
                is_composite INTEGER NOT NULL,
                food_category_id INTEGER,
                FOREIGN KEY (food_category_id) REFERENCES food_categories(id) ON DELETE SET NULL
            )
            """;

    public static final String CREATE_FOOD_COMPONENTS_TABLE = """
            CREATE TABLE IF NOT EXISTS food_components (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                parent_food_item_id INTEGER NOT NULL,
                ingredient_food_item_id INTEGER NOT NULL,
                amount_in_grams REAL NOT NULL,
                FOREIGN KEY (parent_food_item_id) REFERENCES food_items(id) ON DELETE CASCADE,
                FOREIGN KEY (ingredient_food_item_id) REFERENCES food_items(id) ON DELETE RESTRICT
            )
            """;

    public static final String CREATE_FOOD_CATEGORIES_TABLE = """
            CREATE TABLE IF NOT EXISTS food_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            )
            """;

    public static final String CREATE_MEAL_ENTRIES_TABLE = """
            CREATE TABLE IF NOT EXISTS meal_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date DATE NOT NULL,
                time TIME NOT NULL,
                meal_category VARCHAR(50) NOT NULL,
                notes TEXT
            )
            """;

    public static final String CREATE_MEAL_COMPONENTS_TABLE = """
            CREATE TABLE IF NOT EXISTS meal_components (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                meal_entry_id INTEGER NOT NULL,
                food_item_id INTEGER NOT NULL,
                amount_in_grams REAL NOT NULL,
                FOREIGN KEY (meal_entry_id) REFERENCES meal_entries(id) ON DELETE CASCADE,
                FOREIGN KEY (food_item_id) REFERENCES food_items(id) ON DELETE RESTRICT
            )
            """;

    public static final String INSERT_FOOD_ITEM = """
            INSERT INTO food_items (name, calories_per_100g, serving_size_in_grams, unit, proteins_per_100g,
                                    fats_per_100g, carbs_per_100g, is_composite, food_category_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    public static final String INSERT_FOOD_COMPONENT = """
            INSERT INTO food_components (parent_food_item_id, ingredient_food_item_id, amount_in_grams)
            VALUES (?, ?, ?)
            """;

    public static final String INSERT_FOOD_CATEGORY = """
            INSERT INTO food_categories (name)
            VALUES (?)
            """;

    public static final String INSERT_MEAL_ENTRY = """
            INSERT INTO meal_entries (date, time, meal_category, notes)
            VALUES (?, ?, ?, ?)
            """;

    public static final String INSERT_MEAL_COMPONENT = """
            INSERT INTO meal_components (meal_entry_id, food_item_id, amount_in_grams)
            VALUES (?, ?, ?)
            """;

    public static final String SELECT_FOOD_ITEM_BY_ID = """
            SELECT fi.id, fi.name, fi.calories_per_100g, fi.serving_size_in_grams, fi.unit,
                   fi.proteins_per_100g, fi.fats_per_100g, fi.carbs_per_100g, fi.is_composite,
                   fc.id AS category_id, fc.name AS category_name
            FROM food_items fi
            LEFT JOIN food_categories fc
            ON fi.food_category_id = fc.id
            WHERE fi.id = ?
            """;

    public static final String SELECT_FOOD_ITEM_BY_NAME = """
            SELECT fi.id, fi.name, fi.calories_per_100g, fi.serving_size_in_grams, fi.unit,
                   fi.proteins_per_100g, fi.fats_per_100g, fi.carbs_per_100g, fi.is_composite,
                   fc.id AS category_id, fc.name AS category_name
            FROM food_items fi
            LEFT JOIN food_categories fc
            ON fi.food_category_id = fc.id
            WHERE fi.name = ?
            """;

    public static final String SELECT_ALL_FOOD_ITEMS = """
            SELECT fi.id, fi.name, fi.calories_per_100g, fi.serving_size_in_grams, fi.unit,
                   fi.proteins_per_100g, fi.fats_per_100g, fi.carbs_per_100g, fi.is_composite,
                   fc.id AS category_id, fc.name AS category_name
            FROM food_items fi
            LEFT JOIN food_categories fc
            ON fi.food_category_id = fc.id
            """;

    public static final String SELECT_FOOD_COMPONENT = """
            SELECT id, parent_food_item_id, ingredient_food_item_id, amount_in_grams
            FROM food_components
            WHERE parent_food_item_id = ?
            """;

    public static final String SELECT_FOOD_CATEGORY_BY_ID = """
            SELECT id, name
            FROM food_categories
            WHERE id = ?
            """;

    public static final String SELECT_FOOD_CATEGORY_BY_NAME = """
            SELECT id, name
            FROM food_categories
            WHERE name = ?
            """;

    public static final String SELECT_ALL_FOOD_CATEGORY = """
            SELECT id, name
            FROM food_categories
            """;

    public static final String SELECT_MEAL_ENTRY_BY_ID = """
            SELECT id, date, time, meal_category, notes
            FROM meal_entries
            WHERE id = ?
            """;
    public static final String SELECT_MEAL_COMPONENT = """
            SELECT id, meal_entry_id, food_item_id, amount_in_grams
            FROM meal_components
            WHERE meal_entry_id = ?
            """;

    public static final String SELECT_TABLE_NAME = """
            SELECT name
            FROM sqlite_master
            WHERE type = 'table'
            AND name = ?
            """;

    public static final String UPDATE_FOOD_ITEM = """
            UPDATE food_items SET
                name = ?,
                calories_per_100g = ?,
                serving_size_in_grams = ?,
                unit = ?,
                proteins_per_100g = ?,
                fats_per_100g = ?,
                carbs_per_100g = ?,
                is_composite = ?,
                food_category_id = ?
            WHERE id = ?
            """;

    public static final String UPDATE_FOOD_CATEGORY = """
            UPDATE food_categories SET
                name = ?
            WHERE id = ?
            """;

    public static final String UPDATE_MEAL_ENTRY = """
            UPDATE meal_entries SET
                date = ?,
                time = ?,
                meal_category = ?,
                notes = ?
            WHERE id = ?
            """;

    public static final String DELETE_FOOD_ITEM = """
            DELETE FROM food_items
            WHERE id = ?
            """;

    public static final String DELETE_FOOD_CATEGORY = """
            DELETE FROM food_categories
            WHERE id = ?
            """;

    public static final String DELETE_FOOD_COMPONENT = """
            DELETE FROM food_components
            WHERE parent_food_item_id = ?
            """;

    public static final String DELETE_MEAL_ENTRY = """
            DELETE FROM meal_entries
            WHERE id = ?
            """;

    public static final String DELETE_MEAL_COMPONENT = """
            DELETE FROM meal_components
            WHERE meal_entry_id = ?
            """;

    public static final String DROP_TABLE_FOOD_ITEMS = """
            DROP TABLE IF EXISTS food_items
            """;

    public static final String DROP_TABLE_FOOD_COMPONENTS = """
            DROP TABLE IF EXISTS food_components
            """;

    public static final String DROP_TABLE_FOOD_CATEGORIES = """
            DROP TABLE IF EXISTS food_categories
            """;
}