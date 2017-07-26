package de.superioz.moo.manager;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.manager.content.*;
import de.superioz.moo.manager.object.TabBar;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    //
    // SERVER TAB
    //

    @FXML
    public Button addButton, refreshButton;

    @FXML
    public ScrollPane scrollPane;

    private TabBar tabBar;
    private MenuItemConnect menuItemConnect;
    private MenuItemDisconnect menuItemDisconnect;

    @FXML
    public void initialize() {
        this.tabBar = new TabBar(
                new HomeTab(homeButton, homePane, connectionStatus, console),
                new ServerTab(serverButton, serverPane, addButton, refreshButton, scrollPane),
                new ConsoleTab(consoleButton, consolePane, mooConsole, commandInput, commandSend)
        );
        this.menuItemConnect = new MenuItemConnect(connectMenu);
        this.menuItemDisconnect = new MenuItemDisconnect(disconnectMenu);

        // register event listener
        EventExecutor.getInstance().register(tabBar);
    }

}
