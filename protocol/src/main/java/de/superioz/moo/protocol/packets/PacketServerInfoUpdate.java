package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * This packet is for sending server information to the cloud.
 * This is neccessary if the cloud wants to accept server during the offline time
 * of the proxy, so that the proxy can receive every started server to list registered after it
 * starts again.
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketServerInfoUpdate extends AbstractPacket {

    public InetSocketAddress serverAddress;
    public String motd;
    public int onlinePlayers;
    public int maxPlayers;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.serverAddress = new InetSocketAddress(buf.readString(), buf.readInt());
        this.motd = buf.readString();
        this.onlinePlayers = buf.readInt();
        this.maxPlayers = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(serverAddress.getHostName());
        buf.writeInt(serverAddress.getPort());
        buf.writeString(motd);
        buf.writeInt(onlinePlayers);
        buf.writeInt(maxPlayers);
    }
}
