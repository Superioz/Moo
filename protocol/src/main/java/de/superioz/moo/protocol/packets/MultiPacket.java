package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This packet is for inherit multiple packets (of one type) and sending them across
 * the network simultaneously. This can be for example multiple {@link PacketServerRegister} packets.
 *
 * @param <P> The packet type
 */
@NoArgsConstructor
public class MultiPacket<P extends AbstractPacket> extends AbstractPacket {

    @Getter
    private List<P> packetList = new ArrayList<>();
    private Class<P> pClass;

    public MultiPacket(List<P> packets) {
        if(packets.size() != 0) {
            pClass = (Class<P>) packets.get(0).getClass();
        }
        this.packetList = packets;
    }

    public MultiPacket(P... packets) {
        this(Arrays.asList(packets));
    }

    public String getPacketName() {
        return pClass == null ? "?" : pClass.getSimpleName();
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.pClass = (Class<P>) ReflectionUtil.getClass(buf.readString());
        int size = buf.readVarInt();

        for(int i = 0; i < size; i++) {
            P p = (P) ReflectionUtil.getInstance(pClass);
            p.read(buf);
            packetList.add(p);
        }
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(pClass == null ? AbstractPacket.class.getName() : pClass.getName());
        buf.writeVarInt(packetList.size());

        for(P packet : packetList) {
            packet.write(buf);
        }
    }
}
