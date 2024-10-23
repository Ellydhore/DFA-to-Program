module com.example.dfa {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.dfa to javafx.fxml;
    opens com.example.dfa.fxml_controller to javafx.fxml;
    exports com.example.dfa;
    exports com.example.dfa.fxml_controller to javafx.fxml;
}