package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * This packet is for changing the state of a specific player
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayerState extends AbstractPacket {

    public PlayerData data;
    public State state;
    public String meta;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.data = ReflectionUtil.deserialize(buf.readString(), PlayerData.class);
        this.state = buf.readEnumValue(State.class);
        this.meta = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(data.toString());
        buf.writeEnumValue(state);
        buf.writeString(meta);
    }

    /**
     * Listener for this packet
     */
    @Getter
    public static class Event implements de.superioz.moo.api.event.Event {

        private PacketPlayerState packet;

        private InetSocketAddress clientAddress;
        private PlayerData data;

        public Event(PacketPlayerState packet) {
            this.packet = packet;
            this.data = packet.data;
            this.clientAddress = packet.getAddress();
        }

    }

    /**
     * New state of the player
     */
    public enum State {

        /**
         * Player joined a proxy (bungeecord) server and is therefore online
         */
        JOIN_PROXY,

        /**
         * Player left the proxy (bungeecord) and is therefore not longer online
         */
        LEAVE_PROXY,

        /**
         * Player joined a server (spigot)
         */
        JOIN_SERVER,

        /*LEAVE_SERVER,*/

        /**
         * Player connected to a server (bungee)
         */
        CONNECT_SERVER

        /*GET_KICK*/

    }

}
