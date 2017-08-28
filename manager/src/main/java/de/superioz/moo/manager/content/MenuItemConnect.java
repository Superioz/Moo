package de.superioz.moo.manager.content;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.client.events.CloudDisconnectedEvent;
import de.superioz.moo.manager.object.CustomMenuItem;
import de.superioz.moo.netty.client.ClientType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.util.Pair;

import java.util.Optional;

public class MenuItemConnect extends CustomMenuItem implements EventListener {

    public MenuItemConnect(MenuItem item) {
        super(item);

        EventExecutor.getInstance().register(this);
    }

    @Override
    public void handle(ActionEvent event) {
        Optional<Pair<String, String>> opt = new MooConnectDialog().showAndWait();
        if(!opt.isPresent()) return;

        Pair<String, String> pair = opt.get();
        String host = pair.getKey();
        String port = pair.getValue();

        // check values
        if(!Validation.IP.matches(host)) {
            new Alert(Alert.AlertType.ERROR, "The host needs to be a valid IP format!").showAndWait();
            return;
        }
        if(!Validation.INTEGER.matches(port)) {
            new Alert(Alert.AlertType.ERROR, "The port needs to be a valid integer format!").showAndWait();
            return;
        }

        if(Moo.getInstance().isConnected()) return;
        Moo.getInstance().connect("manager", ClientType.INTERFACE, host, Integer.parseInt(port));
    }

    @EventHandler
    public void onMooConnect(CloudConnectedEvent event){
        Platform.runLater(() -> super.getItem().setDisable(true));
    }

    @EventHandler
    public void onMooDisconnect(CloudDisconnectedEvent event){
        Platform.runLater(() -> super.getItem().setDisable(false));
    }

}
