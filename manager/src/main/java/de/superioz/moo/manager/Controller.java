package de.superioz.moo.manager;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.manager.inst.HomeTab;
import de.superioz.moo.manager.inst.MenuItemConnect;
import de.superioz.moo.manager.object.Tab;
import de.superioz.moo.manager.object.TabBar;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;

@Getter
public class Controller {

    //
    // BASIC
    //

    @FXML
    public Button homeButton, serverButton;

    @FXML
    public AnchorPane homePane, serverPane;

    //
    // MENU
    //

    @FXML
    public MenuItem connectMenu;

    //
    // HOME TAB
    //

    @FXML
    public TextField connectionStatus, commandInput;

    @FXML
    public TextArea console;

    @FXML
    public Button commandEnter;

    //

    private TabBar tabBar;

    private MenuItemConnect menuItemConnect;

    @FXML
    public void initialize() {
        this.tabBar = new TabBar(
                new HomeTab(homeButton, homePane, connectionStatus, console, commandInput, commandEnter),
                new Tab(serverButton, serverPane)
        );
        this.menuItemConnect = new MenuItemConnect(connectMenu);
        EventExecutor.getInstance().register(tabBar);
    }

}
