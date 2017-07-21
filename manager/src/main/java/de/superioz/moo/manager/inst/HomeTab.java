package de.superioz.moo.manager.inst;

import de.superioz.moo.manager.object.Tab;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import lombok.Getter;

@Getter
public class HomeTab extends Tab {

    private TextField connectionStatus;
    private TextArea console;
    private TextField commandInput;
    private Button commandEnter;

    public HomeTab(Button button, Pane pane, TextField connectionStatus, TextArea console, TextField commandInput, Button commandEnter) {
        super(button, pane);
        this.connectionStatus = connectionStatus;
        this.console = console;
        this.commandInput = commandInput;
        this.commandEnter = commandEnter;
    }


}
