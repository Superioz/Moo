package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.minecraft.util.ChatUtil;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketPlayerKick;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class PacketPlayerKickListener implements PacketAdapter {

    @PacketHandler
    public void onPlayerKick(PacketPlayerKick packet) {
        String from = packet.from;
        String id = packet.id;
        String message = packet.message;

        // list the proxied player from id
        // if player not found then return bad response
        ProxiedPlayer player = Validation.UNIQUEID.matches(id)
                ? ProxyServer.getInstance().getPlayer(UUID.fromString(id))
                : ProxyServer.getInstance().getPlayer(id);
        if(player == null) {
            packet.respond(ResponseStatus.NOK);
            return;
        }

        // list the playerData from deserializen "from"
        // because we want to check if he is allowed
        PlayerData data;
        if(!from.isEmpty()) {
            // deserialisation
            data = ReflectionUtil.deserialize(from, PlayerData.class);
            if(data == null){
                packet.respond(ResponseStatus.BAD_REQUEST);
                return;
            }

            // checks both ranks to check allowement
            if(MooQueries.getInstance().getGroup(data.getUuid()).getRank()
                    <= MooQueries.getInstance().getGroup(player.getUniqueId()).getRank()) {
                packet.respond(ResponseStatus.FORBIDDEN);
                return;
            }
        }

        // execute kick
        player.disconnect(TextComponent.fromLegacyText(ChatUtil.fabulize(message)));
        packet.respond(ResponseStatus.OK);
    }

}
