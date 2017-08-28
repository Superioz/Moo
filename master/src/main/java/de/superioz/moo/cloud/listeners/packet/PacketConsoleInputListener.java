package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketHandler;
import de.superioz.moo.netty.packets.PacketConsoleInput;

/**
 * This class listens on console input of other instances ({@link de.superioz.moo.netty.client.ClientType#INTERFACE})
 */
public class PacketConsoleInputListener implements PacketAdapter {

    @PacketHandler
    public void onConsoleInput(PacketConsoleInput packet) {
        // use the command line consumer to execute the input
        Cloud.getInstance().getCommandTerminal().getTerminalTask().getNewLine().accept(packet.commandline);
    }

}
