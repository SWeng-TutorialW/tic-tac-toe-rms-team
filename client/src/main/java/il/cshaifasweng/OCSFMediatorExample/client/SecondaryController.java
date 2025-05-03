package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class SecondaryController {

    @FXML private TextField portField;
    @FXML private TextField hostField;

    @FXML
    void handleConnect(ActionEvent event) {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            String host = hostField.getText().trim();

            SimpleClient.overrideConfig(host, port);
            SimpleClient.getClient().openConnection();

            App.setRoot("primary"); // Switch to game screen

        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Connection Failed");
            alert.setHeaderText("Could not connect to server.");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }
}
