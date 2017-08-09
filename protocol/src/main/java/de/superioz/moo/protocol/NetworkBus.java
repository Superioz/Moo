package de.superioz.moo.protocol;

import com.google.common.base.Charsets;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketAdapting;
import de.superioz.moo.protocol.packets.MultiPacket;
import de.superioz.moo.protocol.packets.PacketHandshake;
import de.superioz.moo.protocol.packets.PacketRespond;
import de.superioz.moo.protocol.server.NetworkServer;
import io.netty.channel.Channel;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class for handling incoming and outgoing packets
 */
public class NetworkBus {

    @Getter
    private AbstractNetworkInstance handle;

    public NetworkBus(AbstractNetworkInstance handle) {
        this.handle = handle;
    }

    public void processIn(Channel channel, AbstractPacket packet) {
        // call request/response system
        // Also the packets request/response system
        //UUID packetQuid = packet.getQueryUid();
        if(channel != null) {
            /*String id = (packet.getQueryUid() + "").substring(0, 2);
            id = ConsoleColor.translateLowSpectrum('&', "&" + (id.substring(0, 1))) + id + ConsoleColor.RESET;

            handle.getLogger().info("[Incoming " + id + "] '" + packet.getName() + "'");*/
        }

        // call handler event
        if(packet instanceof PacketHandshake) {
            handle.callEvent(adapter -> adapter.onHandshakeReceive((PacketHandshake) packet));
        }
        else if(handle instanceof NetworkServer) {
            NetworkServer server = (NetworkServer) handle;

            if(channel != null
                    && !server.getClientManager().contains((InetSocketAddress) channel.remoteAddress())) {
                packet.respond(new PacketRespond(ResponseStatus.FORBIDDEN));
                return;
            }
        }

        // call handler event
        handle.callEvent(adapter -> adapter.onPacketReceive(packet));

        if(handle.getCallbacks().asMap().containsKey(packet.getQueryUid())) {
            List<Consumer<AbstractPacket>> callbacks = handle.getCallbacks().getIfPresent(packet.getQueryUid());
            if(callbacks != null) {
                callbacks.forEach(consumer -> consumer.accept(packet));
            }
        }

        // if the packet includes multiple packets (= MultiPacket)
        if(packet instanceof MultiPacket) {
            final ResponseStatus[] status = new ResponseStatus[]{null};
            List<AbstractPacket> packetList = ((MultiPacket<AbstractPacket>) packet).getPacketList();
            handle.getLogger().info("Received multi packet(" + ((MultiPacket) packet).getPacketName() + "). " +
                    "Divided it into " + packetList.size() + " sub-packets.");

            for(AbstractPacket subPacket : packetList) {
                subPacket.interceptRespond(packet1 -> {
                    if(packet1 instanceof PacketRespond) {
                        status[0] = ((PacketRespond) packet1).status;
                    }
                });
                processIn(null, subPacket);

                if(status[0] != null && status[0] != ResponseStatus.OK) break;
            }
            if(status[0] != null) packet.respond(status[0]);
        }

        // call packets processing
        PacketAdapting.getInstance().execute(packet);
    }

    public void processOut(Channel channel, AbstractPacket packet, Consumer<AbstractPacket>... callbacks) {
        if(channel == null) {
            return;
        }

        // send time and identifier
        packet.setStamp(System.currentTimeMillis());
        if(packet.getQueryUid() == null) packet.setQueryUid(UUID.nameUUIDFromBytes(("Time:" + System.nanoTime()).getBytes(Charsets.UTF_8)));
        channel.writeAndFlush(packet);

        // callbacks
        if(callbacks.length != 0) {
            handle.getCallbacks().put(packet.getQueryUid(), new ArrayList<>(Arrays.asList(callbacks)));
        }

        // call handler event
        handle.callEvent(adapter -> adapter.onPacketSend(packet));

        // packet content
        /*String content = (packet instanceof PacketRespond ? " {" + packet.toString().split("\"payload\": ")[1] : "");
        String id = (packet.getQueryUid() + "").substring(0, 2);
        id = ConsoleColor.translateLowSpectrum('&', "&" + (id.substring(0, 1))) + id + ConsoleColor.RESET;

        handle.getLogger().info("[Outgoing " + id + "] '" + packet.getName() + "'" + content);*/
    }

}
