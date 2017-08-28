package de.superioz.moo.netty.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import de.superioz.moo.netty.packet.AbstractPacket;
import de.superioz.moo.netty.packet.PacketBuffer;

import java.io.IOException;

/**
 * This packet is for sending a message across the network (private, ..)
 *
 * @see Type
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayerMessage extends AbstractPacket {

    public Type type;
    public String message;
    public String meta;

    @Setter public boolean colored = true;
    @Setter public boolean formatted = true;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        type = buf.readEnumValue(Type.class);
        message = buf.readString();
        meta = buf.readString();
        colored = buf.readBoolean();
        formatted = buf.readBoolean();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(type);
        buf.writeString(message);
        buf.writeString(meta);
        buf.writeBoolean(colored);
        buf.writeBoolean(formatted);
    }

    /**
     * The type of the message (across the network)
     */
    public enum Type {

        /**
         * Broadcast for every player on the network
         */
        BROADCAST,

        /**
         * Private message to only one player (meta=playerId)
         */
        PRIVATE,

        /**
         * Message to only players with given perm (meta=permission)
         */
        RESTRICTED_PERM,

        /**
         * Message to only players with given rank (meta=rank)
         */
        RESTRICTED_RANK

    }

}
