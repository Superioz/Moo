package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;

/**
 * This packet is for sending server information to the cloud
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketServerInfoUpdate extends AbstractPacket {

    public String motd;
    public int onlinePlayers;
    public int maxPlayers;
    public List<String> players;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.motd = buf.readString();
        this.onlinePlayers = buf.readInt();
        this.maxPlayers = buf.readInt();
        this.players = buf.readStringList();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(motd);
        buf.writeInt(onlinePlayers);
        buf.writeInt(maxPlayers);
        buf.writeStringList(players);
    }
}
