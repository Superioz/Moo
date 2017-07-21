package de.superioz.moo.manager.object;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.manager.events.ChangeTabEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import lombok.Getter;

@Getter
public class Tab {

    private Button button;
    private Pane pane;

    public Tab(Button button, final Pane pane) {
        this.button = button;
        this.pane = pane;

        this.button.setOnAction(event
                -> EventExecutor.getInstance().execute(new ChangeTabEvent(Tab.this)));
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
