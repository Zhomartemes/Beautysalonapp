module com.example.beautysalonapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.beautysalonapp to javafx.fxml;
    exports com.example.beautysalonapp;
}