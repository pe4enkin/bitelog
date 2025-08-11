package com.github.pe4enkin.bitelog;

import com.github.pe4enkin.bitelog.controller.MainViewController;
import com.github.pe4enkin.bitelog.dao.FoodCategoryDao;
import com.github.pe4enkin.bitelog.dao.FoodItemDao;
import com.github.pe4enkin.bitelog.dao.MealEntryDao;
import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.AppState;
import com.github.pe4enkin.bitelog.model.FoodItem;
import com.github.pe4enkin.bitelog.service.DailyDiaryService;
import com.github.pe4enkin.bitelog.service.FoodItemService;
import com.github.pe4enkin.bitelog.service.MealEntryService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

public class MainApp extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);
    private AppState appState;
    private FoodItemService foodItemService;
    private MealEntryService mealEntryService;
    private DailyDiaryService dailyDiaryService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        DataSource dataSource;

        try {
            dataSource = DatabaseConnectionManager.getDataSource();
            try (Connection testConnection = dataSource.getConnection()) {
                LOGGER.info("Успешно подключено к базе данных. Приложение готово к запуску UI.");
            }
            FoodItemDao foodItemDao = new FoodItemDao(dataSource);
            MealEntryDao mealEntryDao = new MealEntryDao(dataSource);
            foodItemDao.createTables();
            mealEntryDao.createTables();
            foodItemService = new FoodItemService(foodItemDao);
            mealEntryService = new MealEntryService(mealEntryDao, foodItemService);
            dailyDiaryService = new DailyDiaryService(mealEntryService);

            appState = new AppState();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/github/pe4enkin/bitelog/view/main-view.fxml"));
            loader.setControllerFactory(type -> {
                if (type == MainViewController.class) {
                    return new MainViewController(appState, foodItemService, dailyDiaryService);
                } else {
                    try {
                        return type.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Не удалось создать контроллер: " + type.getName(), e);
                    }
                }
            });
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/com/github/pe4enkin/bitelog/styles/application.css").toExternalForm());
            primaryStage.setTitle("BiteLog");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (Exception e) {
            LOGGER.error("Критическая ошибка соединения с базой данных.", e);
            javafx.application.Platform.exit();
            System.exit(1);
        }
    }

    public void stop() throws Exception {
        DatabaseConnectionManager.closeDataSource();
        LOGGER.info("Приложение BiteLog завершает работу.");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}