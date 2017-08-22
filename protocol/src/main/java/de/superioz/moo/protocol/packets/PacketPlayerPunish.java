package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.common.punishment.BanSubType;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

@NoArgsConstructor
public class PacketPlayerPunish extends AbstractPacket {

    public String executor;
    public String target;
    public Type type;
    public List<String> meta = new ArrayList<>();

    public PacketPlayerPunish(String executor, String target, Type type, List<String> meta) {
        this.executor = executor;
        this.target = target;
        this.type = type;
        this.meta = meta;
    }

    /**
     * Constructor for a kick punishment
     *
     * @param executor The executor (null if console; uuid if player)
     * @param target   The target
     * @param message  The message
     */
    public PacketPlayerPunish(UUID executor, String target, String message) {
        this(executor + "", target, Type.KICK, Collections.singletonList(message));
    }

    /**
     * Constructor for a ban punishment
     *
     * @param executor       The executor (null if console; uuid if player)
     * @param target         The target to be banned (name or uuid as string)
     * @param banSubType     The sub type of the ban
     * @param reason         The reason
     * @param banTempMessage The format of the temp ban message (temporary ban)
     * @param banPermMessage The format of the perm ban message (permanent ban)
     */
    public PacketPlayerPunish(UUID executor, String target, BanSubType banSubType, String reason,
                              String banTempMessage, String banPermMessage) {
        this(executor + "", target, Type.BAN, Arrays.asList(
                new Ban(executor, banSubType, reason).toString(), banTempMessage, banPermMessage)
        );
    }

    public PacketPlayerPunish(UUID executor, String target, BanSubType banSubType, String reason,
                              long duration, String banTempMessage, String banPermMessage) {
        this(executor + "", target, Type.BAN, Arrays.asList(
                new Ban(executor, banSubType, reason, duration).toString(), banTempMessage, banPermMessage)
        );
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.executor = buf.readString();
        this.target = buf.readString();
        this.type = buf.readEnumValue(Type.class);
        this.meta = buf.readStringList();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(executor);
        buf.writeString(target);
        buf.writeEnumValue(type);
        buf.writeStringList(meta);
    }


    /**
     * The type of the punishment
     */
    public enum Type {

        KICK,
        BAN,
        UNKNOWN

    }

    /**
     * Listener for this packet
     */
    @Getter
    public static class Event implements de.superioz.moo.api.event.Event {

        private PacketPlayerPunish packet;

        private InetSocketAddress clientAddress;
        private PlayerData data;

        public Event(PacketPlayerPunish packet, PlayerData data) {
            this.packet = packet;
            this.clientAddress = packet.getAddress();
            this.data = data;
        }

    }

}
