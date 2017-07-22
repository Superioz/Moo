package de.superioz.moo.manager;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.manager.inst.ConsoleTab;
import de.superioz.moo.manager.inst.HomeTab;
import de.superioz.moo.manager.inst.MenuItemConnect;
import de.superioz.moo.manager.inst.MenuItemDisconnect;
import de.superioz.moo.manager.object.Tab;
import de.superioz.moo.manager.object.TabBar;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import lombok.Getter;

@Getter
public class Controller {

    //
    // BASIC
    //

    @FXML
    public Button homeButton, serverButton, consoleButton;

    @FXML
    public AnchorPane homePane, serverPane, consolePane;

    //
    // MENU
    //

    @FXML
    public MenuItem connectMenu;

    @FXML
    public MenuItem disconnectMenu;

    //
    // HOME TAB
    //

    @FXML
    public TextField connectionStatus;

    @FXML
    public TextArea console;

    public TextFlow flow;

    //
    // CONSOLE TAB
    //

    @FXML
    public TextArea mooConsole;

    @FXML
    public TextField commandInput;

    @FXML
    public Button commandSend;

    private TabBar tabBar;
    private MenuItemConnect menuItemConnect;
    private MenuItemDisconnect menuItemDisconnect;

    @FXML
    public void initialize() {
        this.tabBar = new TabBar(
                new HomeTab(homeButton, homePane, connectionStatus, console),
                new Tab(serverButton, serverPane),
                new ConsoleTab(consoleButton, consolePane, mooConsole, commandInput, commandSend)
        );
        this.menuItemConnect = new MenuItemConnect(connectMenu);
        this.menuItemDisconnect = new MenuItemDisconnect(disconnectMenu);

        // register event listener
        EventExecutor.getInstance().register(tabBar);
    }

}
