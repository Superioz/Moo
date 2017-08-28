package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.netty.client.ClientType;
import de.superioz.moo.netty.common.PacketMessenger;
import de.superioz.moo.netty.common.ResponseStatus;
import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketHandler;
import de.superioz.moo.netty.packets.PacketPlayerMessage;

import java.util.UUID;

/**
 * This class listens to the PacketPlayerMessage (sending a player a private/global message)
 */
public class PacketPlayerMessageListener implements PacketAdapter {

    @PacketHandler
    public void onPlayerMessage(PacketPlayerMessage packet) {
        PacketPlayerMessage.Type type = packet.type;

        // if the type of message is a private message
        // only send the message to this specific player on his server
        boolean privateMessage = type == PacketPlayerMessage.Type.PRIVATE;
        Reaction.react(privateMessage, () -> {
            String id = packet.meta;

            // list player from id
            // if the player
            PlayerData player = Validation.UNIQUEID.matches(id)
                    ? Cloud.getInstance().getMooProxy().getPlayer(UUID.fromString(id))
                    : Cloud.getInstance().getMooProxy().getPlayer(id);
            if(player == null) {
                packet.respond(ResponseStatus.NOT_FOUND);
            }

            // send message
            Cloud.getInstance().getMooProxy().sendMessage(player, packet.deepCopy(), response -> packet.respond(response.getStatus()));
        });

        // if the type of message is not a private message
        Reaction.react(!privateMessage, () -> {
            PacketMessenger.message(packet.deepCopy(), ClientType.PROXY);
            packet.respond(ResponseStatus.OK);
        });
    }

}
