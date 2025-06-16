package com.github.pe4enkin.bitelog.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainViewController {
    @FXML
    private Label label;

    @FXML
    private void handleClick() {
        label.setText("You clicked the button!");
    }
}
