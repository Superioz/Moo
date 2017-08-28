package de.superioz.moo.netty.packet;

import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.netty.AbstractNetworkInstance;
import de.superioz.moo.netty.exception.BadPacketException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Decodes a {@link AbstractPacket}
 */
public class PacketDecoder extends ByteToMessageDecoder {

    /**
     * The netty instance
     */
    private AbstractNetworkInstance main;

    /**
     * The protocolVersion. Default value is -1, but would mean a failure
     */
    @Setter
    private int protocolVersion = -1;

    public PacketDecoder(AbstractNetworkInstance main) {
        this.setSingleDecode(true);
        this.main = main;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> output) {
        try {
            if(buf.readableBytes() == 0) {
                throw new BadPacketException("No readable bytes inside packet!");
            }

            PacketBuffer buffer = new PacketBuffer(buf);

            // read header from buffer
            // like the protocol version and id
            int protocolVersion = buffer.readVarInt();
            int protocolId = buffer.readVarInt();
            UUID queryUid = buffer.readUuid();
            long timestamp = buffer.readLong();

            // check protocol
            if(this.protocolVersion != protocolVersion) {
                buf.skipBytes(buf.readableBytes());
                throw new BadPacketException("Received packets with wrong protocol version! " + protocolVersion + " instead of " + this.protocolVersion);
            }
            Class<? extends AbstractPacket> pClass = main.getRegistry().getPacket(protocolId);
            if(pClass == null) {
                buf.skipBytes(buf.readableBytes());
                throw new BadPacketException("Cannot receive unregistered packets! (id:" + protocolId + ")");
            }

            // builds the packets from the values
            // if the packet is null the constructor must be null
            AbstractPacket packet = (AbstractPacket) ReflectionUtil.getInstance(pClass);
            if(packet == null) {
                throw new NullPointerException("Packet is null because there is no NoArgsConstructor inside " + pClass.getSimpleName() + "!");
            }
            packet.protocolVersion = protocolVersion;
            packet.protocolId = protocolId;
            packet.queryUid = queryUid;
            packet.stamp = timestamp;
            packet.channel = ctx.channel();
            packet.buf = buf;

            // makes the packets reads the payload from the packetbuffer
            try {
                packet.read(buffer);
            }
            catch(Exception e) {
                System.err.println("Error inside " + packet.getName() + "#read method: " + e.getClass().getSimpleName());
                e.printStackTrace();
            }
            output.add(packet);
        }
        catch(Exception e) {
            System.err.println("Error while decoding packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
