package de.superioz.moo.network.packets;

import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * Send this packet to inform daemon about pattern creation/deletion<br>
 * All patterns will be in the redis, so if the daemon starts if fetches the
 * patterns from there
 */
@AllArgsConstructor
@NoArgsConstructor
public class PacketPatternState extends AbstractPacket {

    /**
     * The name of the pattern
     *
     * @see ServerPattern#name
     */
    public String name;

    /**
     * The state of the pattern
     * true = created; false = deleted
     */
    public boolean state;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.name = buf.readString();
        this.state = buf.readBoolean();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(name);
        buf.writeBoolean(state);
    }
}
