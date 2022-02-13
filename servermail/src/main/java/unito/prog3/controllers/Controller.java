package unito.prog3.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class Controller {
    @FXML
    private TextArea log_area;

    public void log(String text) {
        log_area.appendText(text);
    }
}
