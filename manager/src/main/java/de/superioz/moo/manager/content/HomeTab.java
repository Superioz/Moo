package de.superioz.moo.manager.content;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.events.MooLoggingEvent;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.client.events.CloudDisconnectedEvent;
import de.superioz.moo.manager.Manager;
import de.superioz.moo.manager.object.Tab;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import lombok.Getter;

@Getter
public class HomeTab extends Tab implements EventListener {

    private static final String NOT_CONNECTED = "Du bist nicht mit der Cloud verbunden. Benutze \"Connection\" > \"Connect ..\"";
    private static final String CONNECTED = "Du bist mit der Cloud verbunden.";

    private TextField connectionStatus;
    private TextArea console;

    public HomeTab(Button button, Pane pane, TextField connectionStatus, TextArea console) {
        super(button, pane);
        this.connectionStatus = connectionStatus;
        this.console = console;

        // set connection status to red, because no connection exists
        connectionStatus.setText(NOT_CONNECTED);
        connectionStatus.setStyle("-fx-text-inner-color: red;");

        // register this as event listener
        EventExecutor.getInstance().register(this);
    }

    @EventHandler
    public void onMooLog(MooLoggingEvent event) {
        Platform.runLater(() -> console.appendText(event.getMessage()));
    }

    @EventHandler
    public void onMooConnect(CloudConnectedEvent event) {
        Manager.getInstance().getLogger().info("** AUTHENTICATION STATUS: " + (event.getStatus()) + " **");

        Platform.runLater(() -> {
            connectionStatus.setText(CONNECTED);
            connectionStatus.setStyle("-fx-text-inner-color: green;");
        });
    }

    @EventHandler
    public void onMooDisconnect(CloudDisconnectedEvent event) {
        Platform.runLater(() -> {
            connectionStatus.setText(NOT_CONNECTED);
            connectionStatus.setStyle("-fx-text-inner-color: red;");
        });
    }

}
