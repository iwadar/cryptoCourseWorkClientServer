module com.example.clientwithui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.clientwithui to javafx.fxml;
    exports com.example.clientwithui;
}