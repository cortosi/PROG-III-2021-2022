package unito.prog3.servermail;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import unito.prog3.controllers.Controller;

import java.io.IOException;


public class MailServer extends Application {
    @Override
    public void start(Stage stage) {
        // Creating the FXMLLoader
        FXMLLoader fxmlLoader = new FXMLLoader(MailServer.class.getResource("views/primary.fxml"));
        // Setting up scene
        Controller controller = new Controller();
        fxmlLoader.setController(controller);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load(), 1024, 600);
            stage.setScene(scene);
            // Stylesheets
            scene.getStylesheets().add(MailServer.class.getResource("style/style.css").toExternalForm());
            stage.show();
            new Thread(new Server(1998, controller)).start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
