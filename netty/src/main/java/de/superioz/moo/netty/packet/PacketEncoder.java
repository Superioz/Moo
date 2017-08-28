package de.superioz.moo.netty.packet;

import de.superioz.moo.netty.AbstractNetworkInstance;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Setter;

/**
 * Encodes a {@link AbstractPacket}
 */
public class PacketEncoder extends MessageToByteEncoder<AbstractPacket> {

    /**
     * The netty instance
     */
    private AbstractNetworkInstance main;

    /**
     * The protocolVersion. Default value is -1, but would mean a failure
     */
    @Setter
    private int protocolVersion = -1;

    public PacketEncoder(AbstractNetworkInstance main) {
        this.main = main;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractPacket packet, ByteBuf output) {
        try {
            PacketBuffer buffer = new PacketBuffer(output);

            int protocolId = main.getRegistry().getId(packet.getClass());
            if(protocolId == -1) {
                throw new Exception("Couldn't find packet! (id: " + protocolId + ")");
            }
            packet.protocolVersion = protocolVersion;
            packet.protocolId = protocolId;

            // Writes important values to the header of the packets
            // example is the protocol version and id
            // and the queryuid which is used to determine the pipeline between request/response
            buffer.writeVarInt(protocolVersion);
            buffer.writeVarInt(protocolId);
            buffer.writeUuid(packet.queryUid);
            buffer.writeLong(packet.stamp);

            // message
            try {
                packet.write(buffer);
            }
            catch(Exception e) {
                output.clear();

                System.err.println("Error inside " + packet.getName() + "#write method: " + e.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
        catch(Exception e) {
            System.err.println("Error while encoding " + packet.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

}
