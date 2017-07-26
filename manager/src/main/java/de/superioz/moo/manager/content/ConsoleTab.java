package de.superioz.moo.manager.content;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.client.events.CloudDisconnectedEvent;
import de.superioz.moo.manager.object.Tab;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketAdapting;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketConsoleInput;
import de.superioz.moo.protocol.packets.PacketConsoleOutput;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class ConsoleTab extends Tab implements EventListener, PacketAdapter {

    public TextArea mooConsole;
    public TextField commandInput;
    public Button commandSend;

    public ConsoleTab(Button button, Pane pane, TextArea mooConsole, TextField commandInput, Button commandSend) {
        super(button, pane);
        this.mooConsole = mooConsole;
        this.commandInput = commandInput;
        this.commandSend = commandSend;

        // for sending command
        commandSend.setOnAction(event -> {
            String text = commandInput.getText();
            commandInput.clear();
            PacketMessenger.message(new PacketConsoleInput(text));
        });

        // register this as event listener
        EventExecutor.getInstance().register(this);
        PacketAdapting.getInstance().register(this);
    }

    @PacketHandler
    public void onLogPacket(PacketConsoleOutput packet) {
        Platform.runLater(() -> mooConsole.appendText(packet.message));
    }

    @EventHandler
    public void onMooConnect(CloudConnectedEvent event) {
        Platform.runLater(() -> ConsoleTab.this.getButton().setDisable(false));
    }

    @EventHandler
    public void onMooDisconnect(CloudDisconnectedEvent event) {
        Platform.runLater(() -> {
            mooConsole.clear();
            ConsoleTab.this.getButton().setDisable(true);
        });
    }

}
