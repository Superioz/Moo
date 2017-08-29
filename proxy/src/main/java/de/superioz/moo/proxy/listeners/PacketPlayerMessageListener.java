package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.util.SpecialCharacter;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.netty.common.MooQueries;
import de.superioz.moo.minecraft.chat.MessageComponent;
import de.superioz.moo.minecraft.util.ChatUtil;
import de.superioz.moo.netty.common.ResponseStatus;
import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketHandler;
import de.superioz.moo.netty.packets.PacketPlayerMessage;
import de.superioz.moo.proxy.util.BungeeChat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class PacketPlayerMessageListener implements PacketAdapter {

    @PacketHandler
    public void onPlayerMessage(PacketPlayerMessage packet) {
        PacketPlayerMessage.Type type = packet.type;
        String meta = packet.meta;
        String packetMessage = packet.message;
        if(packet.colored) packetMessage = ChatUtil.colored(packetMessage);
        if(packet.formatted) packetMessage = SpecialCharacter.apply(packetMessage);

        TextComponent message = new MessageComponent(packetMessage).formatAll().toTextComponent();

        // if the type is private
        if(type == PacketPlayerMessage.Type.PRIVATE) {
            // list player from meta
            // if null return bad response
            ProxiedPlayer player = Validation.UNIQUEID.matches(meta)
                    ? ProxyServer.getInstance().getPlayer(UUID.fromString(meta))
                    : ProxyServer.getInstance().getPlayer(meta);
            if(player == null) {
                packet.respond(ResponseStatus.NOT_FOUND);
                return;
            }

            // send message to this player
            BungeeChat.send(message, player);
            packet.respond(ResponseStatus.OK);
            return;
        }
        // if the type is to broadcast
        else if(type == PacketPlayerMessage.Type.BROADCAST) {
            BungeeChat.broadcast(message);
            return;
        }

        // if the meta is empty then no chance to
        // determine which player to send to
        if(meta.isEmpty()) {
            BungeeChat.broadcast(message);
            return;
        }

        // if we can determine the target ..
        // checks for permission of the player
        if(type == PacketPlayerMessage.Type.RESTRICTED_PERM) {
            // if the meta is not a permission
            if(!Validation.PERMISSION.matches(meta)) {
                packet.respond(ResponseStatus.BAD_REQUEST);
                return;
            }

            // loop through all players and search for permission
            for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if(player.hasPermission(meta)) {
                    BungeeChat.send(message, player);
                }
            }
        }
        // checks for the rank
        else {
            // if the meta is not a number
            if(!Validation.INTEGER.matches(meta)) {
                packet.respond(ResponseStatus.BAD_REQUEST);
                return;
            }
            int rank = Integer.parseInt(meta);

            // loop through all players and search for rank
            for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                Group group = MooQueries.getInstance().getGroup(player.getUniqueId());

                if(group.getRank() >= rank) {
                    BungeeChat.send(message, player);
                }
            }
        }
    }

}
