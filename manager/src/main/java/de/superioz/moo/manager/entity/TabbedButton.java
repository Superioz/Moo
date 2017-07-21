package de.superioz.moo.manager.entity;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.manager.events.ChangeTabEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import lombok.Getter;

@Getter
public class TabbedButton {

    private Button button;
    private Pane pane;

    public TabbedButton(Button button, final Pane pane) {
        this.button = button;
        this.pane = pane;

        this.button.setOnAction(event
                -> EventExecutor.getInstance().execute(new ChangeTabEvent(TabbedButton.this)));
    }

    /**
     * Checks if the pane (this tab) is visible
     *
     * @return The result
     */
    public boolean isVisible() {
        return pane.isVisible();
    }

    /**
     * Toggles the visibility of the tab
     */
    public void toggle() {
        pane.setVisible(!isVisible());
    }

}
