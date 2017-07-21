package de.superioz.moo.manager.object;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import lombok.Getter;

@Getter
public abstract class CustomMenuItem {

    private MenuItem item;

    public CustomMenuItem(MenuItem item) {
        this.item = item;
        this.item.setOnAction(CustomMenuItem.this::handle);
    }

    public abstract void handle(ActionEvent event);

}
