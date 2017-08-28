package de.superioz.moo.daemon.listeners;

import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketPatternState;

public class PacketPatternStateListener implements PacketAdapter {

    @PacketHandler
    public void onPatternState(PacketPatternState packet) {
        String patternName = packet.name;
        boolean state = packet.state;

        if(state) {
            Daemon.getInstance().getServer().createPattern(patternName);
        }
        else {
            Daemon.getInstance().getServer().deletePattern(patternName);
        }
        Daemon.getInstance().getServer().fetchPatterns();
    }

}
