package de.superioz.moo.manager.content;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.client.events.CloudDisconnectedEvent;
import de.superioz.moo.manager.object.CustomMenuItem;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;

public class MenuItemDisconnect extends CustomMenuItem implements EventListener {

    public MenuItemDisconnect(MenuItem item) {
        super(item);

        EventExecutor.getInstance().register(this);
    }

    @Override
    public void handle(ActionEvent event) {
        if(!Moo.getInstance().isConnected()) return;
        Moo.getInstance().disconnect();
    }

    @EventHandler
    public void onMooConnect(CloudConnectedEvent event){
        Platform.runLater(() -> super.getItem().setDisable(false));
    }

    @EventHandler
    public void onMooDisconnect(CloudDisconnectedEvent event){
        Platform.runLater(() -> super.getItem().setDisable(true));
    }

}
