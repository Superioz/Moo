package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.network.common.PacketMessenger;
import de.superioz.moo.network.queries.ResponseStatus;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketRequest;

import java.util.UUID;

public class PacketRequestListener implements PacketAdapter {

    @PacketHandler
    public void onRequest(PacketRequest packet) {
        PacketRequest.Type type = packet.type;
        String information = packet.meta;

        // I want to know the ping of the player
        Reaction.react(type, PacketRequest.Type.PING, () -> {
            PlayerData player = Validation.UNIQUEID.matches(information) ?
                    Cloud.getInstance().getNetworkProxy().getPlayer(UUID.fromString(information))
                    : Cloud.getInstance().getNetworkProxy().getPlayer(information);

            // didn't found the player
            if(player == null) {
                packet.respond(ResponseStatus.NOT_FOUND);
                return;
            }

            // sends a request to fetch the players ping
            PacketMessenger.message(packet, response -> packet.respond(response.getHandle()),
                    Cloud.getInstance().getNetworkProxy().getClient(player));
        });
    }

}
