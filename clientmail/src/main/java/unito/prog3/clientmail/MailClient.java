package unito.prog3.clientmail;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import unito.prog3.controllers.Controller;

import java.io.IOException;


public class MailClient extends Application {
    @Override
    public void start(Stage stage) {
        // Creating the FXMLLoader
        FXMLLoader fxmlLoader = new FXMLLoader(MailClient.class.getResource("views/primary.fxml"));
        // Setting up scene
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load(), 1024, 600);
            stage.setScene(scene);
            // Stylesheets
            scene.getStylesheets().add(MailClient.class.getResource("style/Style.css").toExternalForm());
            stage.show();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
