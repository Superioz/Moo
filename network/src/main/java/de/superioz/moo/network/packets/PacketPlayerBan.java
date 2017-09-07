package de.superioz.moo.network.packets;

import de.superioz.moo.api.common.punishment.BanCategory;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayerBan extends AbstractPacket {

    public UUID executor;
    public String target;
    public Ban ban;
    public String banTempMessage;
    public String banPermMessage;

    /**
     * Constructor for a ban punishment
     *
     * @param executor       The executor (null if console; uuid if player)
     * @param target         The target to be banned (name or uuid as string)
     * @param banSubType     The sub type of the ban
     * @param reason         The reason
     * @param duration       The duration
     * @param banTempMessage The format of the temp ban message (temporary ban)
     * @param banPermMessage The format of the perm ban message (permanent ban)
     */
    public PacketPlayerBan(UUID executor, String target, BanCategory banSubType, String reason,
                           long duration, String banTempMessage, String banPermMessage) {
        this(executor, target, new Ban(executor, banSubType, reason, duration), banTempMessage, banPermMessage);
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.executor = buf.readUuid();
        this.target = buf.readString();
        this.ban = ReflectionUtil.deserialize(buf.readString(), Ban.class);
        this.banTempMessage = buf.readString();
        this.banPermMessage = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeUuid(executor);
        buf.writeString(target);
        buf.writeString(ban.toString());
        buf.writeString(banTempMessage);
        buf.writeString(banPermMessage);
    }

}
