package de.superioz.moo.cloud.listeners;

import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketConsoleInput;

public class PacketConsoleInputListener implements PacketAdapter {

    @PacketHandler
    public void onConsoleInput(PacketConsoleInput packet) {
        // use the command line consumer to execute the input
        Cloud.getInstance().getCommandTerminal().getTerminalTask().getNewLine().accept(packet.commandline);
    }

}
