package de.superioz.moo.manager;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.manager.entity.TabBar;
import de.superioz.moo.manager.entity.TabbedButton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;

@Getter
public class Controller {

    @FXML
    public Button homeButton, serverButton;

    @FXML
    public AnchorPane homePane, serverPane;

    private TabBar tabBar;

    @FXML
    public void initialize() {
        this.tabBar = new TabBar(new TabbedButton(homeButton, homePane), new TabbedButton(serverButton, serverPane));
        EventExecutor.getInstance().register(tabBar);
    }

}
