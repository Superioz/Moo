package de.superioz.moo.network.common;

import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packets.PacketHandshake;
import io.netty.channel.Channel;

/**
 * This adapter is for listening to important network instance events
 */
public interface NetworkEventAdapter {

    /**
     * When the instance receives a packet
     *
     * @param packet The packet
     */
    void onPacketReceive(AbstractPacket packet);

    /**
     * When the instance receives a handshake packet
     *
     * @param handshake The handshake packet
     */
    void onHandshakeReceive(PacketHandshake handshake);

    /**
     * When the instance wants to send a packet
     *
     * @param packet The packet
     */
    void onPacketSend(AbstractPacket packet);

    /**
     * When the instance connected to the server or when a client connected to the instance
     *
     * @param channel The channel
     */
    void onChannelActive(Channel channel);

    /**
     * When the instance disconnected from the server or when a client disconnected from the instance
     *
     * @param channel The channel
     */
    void onChannelInactive(Channel channel);

}
