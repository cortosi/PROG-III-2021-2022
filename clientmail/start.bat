@ECHO OFF
java --module-path ./lib/javafx-sdk-17.0.1/lib --add-modules javafx.controls,javafx.fxml -jar ./out/artifacts/clientmail_jar/clientmail.jar
PAUSE