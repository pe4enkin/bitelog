package com.github.pe4enkin.bitelog.sql;

public class SqlQueries {
    public static final String CREATE_FOOD_ITEMS_TABLE = """
            CREATE TABLE IF NOT EXISTS food_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
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
                FOREIGN KEY (ingredient_food_item_id) REFERENCES food_items(id) ON DELETE CASCADE
            )
            """;

    public static final String CREATE_FOOD_CATEGORIES_TABLE = """
            CREATE TABLE IF NOT EXISTS food_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
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

    public static final String SELECT_FOOD_ITEM = """
            SELECT fi.id, fi.name, fi.calories_per_100g, fi.serving_size_in_grams, fi.unit,
                   fi.proteins_per_100g, fi.fats_per_100g, fi.carbs_per_100g, fi.is_composite,
                   fc.id AS category_id, fc.name AS category_name
            FROM food_items fi
            LEFT JOIN food_categories fc
            ON fi.food_category_id = fc.id
            WHERE fi.id = ?
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
            SELECT fc.id, fc.parent_food_item_id, fc.ingredient_food_item_id, fc.amount_in_grams,
                   fi.name AS component_name
            FROM food_components fc
            JOIN food_items fi
            ON fc.ingredient_food_item_id = fi.id
            WHERE fc.parent_food_item_id = ?
            """;

    public static final String SELECT_FOOD_CATEGORY = """
            SELECT id, name
            FROM food_categories
            WHERE id = ?
            """;

    public static final String SELECT_ALL_FOOD_CATEGORY = """
            SELECT id, name
            FROM food_categories
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
}