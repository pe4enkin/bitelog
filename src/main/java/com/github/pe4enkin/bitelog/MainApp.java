package com.github.pe4enkin.bitelog;

import com.github.pe4enkin.bitelog.controller.MainViewController;
import com.github.pe4enkin.bitelog.db.DatabaseConnectionManager;
import com.github.pe4enkin.bitelog.model.AppState;
import com.github.pe4enkin.bitelog.service.DiaryService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private AppState appState;
    private DiaryService diaryService;
    private Connection dbConnection;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            dbConnection = DatabaseConnectionManager.getConnection();
            logger.info("Успешно подключено к базе данных. Приложение готово к запуску UI.");
            diaryService = new DiaryService();
        } catch (Exception e) {
            logger.error("Критическая ошибка соединения с базой данных.", e);
            javafx.application.Platform.exit();
            System.exit(1);
        }
        appState = new AppState();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/github/pe4enkin/bitelog/view/main-view.fxml"));
        MainViewController controller = new MainViewController(appState, diaryService);
        loader.setController(controller);
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                getClass().getResource("/com/github/pe4enkin/bitelog/styles/application.css").toExternalForm());
        primaryStage.setTitle("BiteLog");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void stop() throws Exception {
        if ((dbConnection != null)) {
            try {
                dbConnection.close();
                logger.info("Соединение с базой данных успешно закрыто.");
            } catch (SQLException e) {
                logger.error("Ошибка при закрытии соединения с базой данных.", e);
            }
        }
        logger.info("Приложение BiteLog завершает работу.");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}