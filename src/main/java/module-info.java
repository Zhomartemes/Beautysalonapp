module com.example.beautysalonapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.beautysalonapp to javafx.fxml;
    exports com.example.beautysalonapp;
}