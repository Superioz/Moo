package de.superioz.moo.manager.inst;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class MooConnectDialog extends Dialog {

    public MooConnectDialog() {
        super.setTitle("Connector");
        super.setHeaderText("Set host and port to connect ..");

        // Set buttons
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        super.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        // Create text fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create labels and fields
        TextField host = new TextField();
        host.setPromptText("Host");
        TextField port = new TextField();
        port.setPromptText("Port");

        grid.add(new Label("Host:"), 0, 0);
        grid.add(host, 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(port, 1, 1);

        // Disable/Enable button
        Node loginButton = super.getDialogPane().lookupButton(connectButtonType);
        loginButton.setDisable(true);

        host.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        super.getDialogPane().setContent(grid);

        // Convert the result to a username-password-pair when the login button is clicked.
        super.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                return new Pair<>(host.getText(), port.getText());
            }
            return null;
        });
    }
}
