package de.superioz.moo.manager.object;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.manager.events.ChangeTabEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import lombok.Getter;

@Getter
public class Tab {

    private static final PseudoClass TAB_ACTIVE_CLASS = PseudoClass.getPseudoClass("tab-active");
    private BooleanProperty active;

    private Button button;
    private Pane pane;

    public Tab(Button button, final Pane pane) {
        this.button = button;
        this.pane = pane;

        this.button.setOnAction(event
                -> EventExecutor.getInstance().execute(new ChangeTabEvent(Tab.this)));
        this.active = new SimpleBooleanProperty(false);
        this.active.addListener((observable, oldValue, newValue) -> {
            button.pseudoClassStateChanged(TAB_ACTIVE_CLASS, active.get());
            pane.pseudoClassStateChanged(TAB_ACTIVE_CLASS, active.get());
        });
    }

    /**
     * Toggles the visibility of the tab
     */
    public void toggle() {
        this.updateStyle(!pane.isVisible());
        active.set(pane.isVisible());
    }

    /**
     * Changes the tab style
     */
    public void updateStyle(boolean visible) {
        pane.setVisible(visible);
    }

}
