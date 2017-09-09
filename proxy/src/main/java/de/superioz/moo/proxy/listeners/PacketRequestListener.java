package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.util.Validation;
import de.superioz.moo.network.queries.ResponseStatus;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketRequest;
import de.superioz.moo.network.packets.PacketRespond;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class PacketRequestListener implements PacketAdapter {

    @PacketHandler
    public void onRequest(PacketRequest packet) {
        PacketRequest.Type type = packet.type;
        String meta = packet.meta;

        // requesting a player's ping
        if(type == PacketRequest.Type.PING) {
            // list the player from the metadata of the packet
            // if player is null then
            ProxiedPlayer player = Validation.UNIQUEID.matches(meta)
                    ? ProxyServer.getInstance().getPlayer(UUID.fromString(meta))
                    : ProxyServer.getInstance().getPlayer(meta);
            if(player == null) {
                packet.respond(new PacketRespond(ResponseStatus.NOT_FOUND));
                return;
            }

            packet.respond(player.getPing() + "");
        }
    }

}
