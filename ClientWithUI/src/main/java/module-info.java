module com.example.clientwithui {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires lombok;

    opens com.example.clientwithui to javafx.fxml;
    exports com.example.clientwithui;
}