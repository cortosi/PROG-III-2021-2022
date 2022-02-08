module unito.prog3.servermail {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;
    requires org.json;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;

    opens unito.prog3.servermail to javafx.fxml, com.fasterxml.jackson.databind;
    opens unito.prog3.models to com.fasterxml.jackson.databind;

    exports unito.prog3.servermail;
}