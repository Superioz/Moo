package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.HandshakeEvent;
import de.superioz.moo.cloud.events.MooClientConnectedEvent;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketHandshake;
import de.superioz.moo.protocol.packets.PacketRespond;
import de.superioz.moo.protocol.server.MooClient;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public class HandshakeListener implements EventListener {

    @EventHandler
    public void onHandshake(HandshakeEvent event) {
        Channel channel = event.getChannel();
        PacketHandshake packet = event.getPacket();

        String header = "auth";
        String version = Cloud.getInstance().getVersion();
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();

        // checks if address is allowed by the whitelist
        if(!Cloud.getInstance().getServer().getWhitelist().allowed(remoteAddress)) {
            packet.respond(new PacketRespond(header, version, ResponseStatus.FORBIDDEN));
            channel.disconnect();
        }

        // checks if the instance name is valid
        if(!Validation.INSTANCE_NAME.matches(packet.identifier)) {
            packet.respond(new PacketRespond(header, version, ResponseStatus.BAD_REQUEST));
            channel.disconnect();
            return;
        }

        // checks if already a daemon from this host connected to the server
        if(packet.type == ClientType.DAEMON && Cloud.getInstance().getHub().contains(remoteAddress)) {
            packet.respond(new PacketRespond(header, version, ResponseStatus.FORBIDDEN));
            channel.disconnect();
            return;
        }

        //.
        packet.respond(new PacketRespond(header, version, ResponseStatus.OK));

        // Add client
        MooClient client = new MooClient(packet.identifier,
                remoteAddress.getAddress().getHostAddress(),
                remoteAddress.getPort(), packet.type, channel);
        client.setId(Cloud.getInstance().getHub().add(client));

        // fire event of client connection
        EventExecutor.getInstance().execute(new MooClientConnectedEvent(client));

        Cloud.getLogger().debug(ConsoleColor.GREEN.toString()
                + client.getType() + " client connected @(" + remoteAddress.getAddress().getHostAddress() + ")");
    }

}
