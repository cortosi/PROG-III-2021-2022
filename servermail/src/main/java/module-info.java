module unito.prog3.servermail {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;

    opens unito.prog3.servermail to javafx.fxml;
    exports unito.prog3.servermail;
}