package com.github.pe4enkin.bitelog;

import com.github.pe4enkin.bitelog.controller.MainViewController;
import com.github.pe4enkin.bitelog.model.AppState;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private AppState appState;

    @Override
    public void start(Stage primaryStage) throws Exception {
        appState = new AppState();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/github/pe4enkin/bitelog/view/main-view.fxml"));
        MainViewController controller = new MainViewController(appState);
        loader.setController(controller);
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                getClass().getResource("/com/github/pe4enkin/bitelog/styles/application.css").toExternalForm());
        primaryStage.setTitle("BiteLog");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}