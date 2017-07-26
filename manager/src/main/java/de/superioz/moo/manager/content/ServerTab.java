package de.superioz.moo.manager.content;

import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.client.events.CloudDisconnectedEvent;
import de.superioz.moo.manager.object.Tab;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

public class ServerTab extends Tab implements EventListener {

    public Button addButton;
    public Button reloadButton;
    public ScrollPane scrollPane;

    public ServerTab(Button button, Pane pane, Button addButton, Button reloadButton, ScrollPane scrollPane) {
        super(button, pane);
        this.addButton = addButton;
        this.reloadButton = reloadButton;
        this.scrollPane = scrollPane;
    }

    @EventHandler
    public void onMooConnect(CloudConnectedEvent event) {
        Platform.runLater(() -> ServerTab.this.getButton().setDisable(false));
    }

    @EventHandler
    public void onMooDisconnect(CloudDisconnectedEvent event) {
        Platform.runLater(() -> {
            ServerTab.this.getButton().setDisable(true);
        });
    }

}
