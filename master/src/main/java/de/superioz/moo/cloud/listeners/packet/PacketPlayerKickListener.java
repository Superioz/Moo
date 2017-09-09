package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.network.queries.ResponseStatus;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketPlayerKick;

/**
 * This class listens to PacketPlayerKick
 */
public class PacketPlayerKickListener implements PacketAdapter {

    @PacketHandler
    public void onPlayerKick(PacketPlayerKick packet) {
        // gets the player from given id
        // if the player cannot be found, than just return a bad status
        PlayerData player = Cloud.getInstance().getNetworkProxy().getPlayer(packet.target);
        if(player == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        Cloud.getInstance().getNetworkProxy().kick(player, packet.deepCopy(), response -> packet.respond(response.getStatus()));
    }

}
