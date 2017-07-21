package de.superioz.moo.manager.inst;

import de.superioz.moo.manager.object.CustomMenuItem;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;

public class MenuItemConnect extends CustomMenuItem {

    public MenuItemConnect(MenuItem item) {
        super(item);
    }

    @Override
    public void handle(ActionEvent event) {
        System.out.println("CONNECT");
    }

}
