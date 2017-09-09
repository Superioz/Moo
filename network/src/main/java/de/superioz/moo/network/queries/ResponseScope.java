package de.superioz.moo.network.queries;

import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packets.PacketRespond;
import lombok.Getter;

/**
 * This class defines the type of object which has to be returned from a query
 */
public enum ResponseScope {

    /**
     * Just any packet
     */
    DEFAULT(AbstractPacket.class),

    /**
     * Only the {@link PacketRespond} packet
     */
    RESPOND(PacketRespond.class),

    /**
     * Only the wrapper object for {@link #RESPOND}: {@link Response}
     */
    RESPONSE;

    /**
     * The class of the has-to-be-returned-packet (only if not {@link #RESPONSE})
     */
    @Getter
    private Class<? extends AbstractPacket> wrappedClass;

    ResponseScope(Class<? extends AbstractPacket> wrappedClass) {
        this.wrappedClass = wrappedClass;
    }

    ResponseScope() {
    }
}
