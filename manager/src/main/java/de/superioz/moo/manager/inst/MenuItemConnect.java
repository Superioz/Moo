package de.superioz.moo.manager.inst;

import de.superioz.moo.manager.object.CustomMenuItem;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.util.Pair;

import java.util.Optional;

public class MenuItemConnect extends CustomMenuItem {

    public MenuItemConnect(MenuItem item) {
        super(item);
    }

    @Override
    public void handle(ActionEvent event) {
        Optional<Pair<String, String>> opt = new MooConnectDialog().showAndWait();
        Pair<String, String> pair = opt.get();
        String host = pair.getKey();
        String port = pair.getValue();
    }

}
