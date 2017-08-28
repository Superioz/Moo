package de.superioz.moo.netty.events;

import de.superioz.moo.netty.packet.AbstractPacket;
import io.netty.channel.Channel;
import lombok.Getter;
import de.superioz.moo.api.event.Event;

import java.util.function.Consumer;

/**
 * The event for adding a packet to the sending queue
 */
@Getter
public class PacketQueueEvent implements Event {

    /**
     * The channel of the packet sender
     */
    private Channel channel;

    /**
     * The packet to be queued
     */
    private AbstractPacket packet;

    /**
     * The callbacks
     */
    private Consumer<AbstractPacket>[] callbacks;

    /**
     * Async packet handling
     */
    private boolean async = false;

    public PacketQueueEvent(Channel channel, AbstractPacket packet, Consumer<AbstractPacket>... callbacks) {
        this.channel = channel;
        this.packet = packet;
        this.callbacks = callbacks;
    }

    /**
     * Sets the {@link #async}
     *
     * @param r The new value
     * @return This
     */
    public PacketQueueEvent async(boolean r) {
        this.async = r;
        return this;
    }

}
