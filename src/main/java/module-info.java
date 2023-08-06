module com.example.network1hw2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.network1hw2 to javafx.fxml;
    exports com.example.network1hw2;
}