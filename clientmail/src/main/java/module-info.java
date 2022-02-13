module unito {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;

    opens unito.prog3.clientmail to javafx.fxml;
    opens unito.prog3.controllers to javafx.fxml;

    exports unito.prog3.clientmail;
    exports unito.prog3.models;

    opens unito.prog3.models to javafx.fxml;
}