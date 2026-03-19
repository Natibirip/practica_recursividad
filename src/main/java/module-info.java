module com.example.practica_recursividad {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.practica_recursividad to javafx.fxml;
    exports com.example.practica_recursividad;
}