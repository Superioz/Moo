package de.superioz.moo.proxy.listeners;

import de.superioz.moo.minecraft.util.ChatUtil;
import de.superioz.moo.network.common.ResponseStatus;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketPlayerKick;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class PacketPlayerKickListener implements PacketAdapter {

    @PacketHandler
    public void onPlayerKick(PacketPlayerKick packet) {
        UUID target = packet.target;
        String message = packet.message;

        // list the proxied player from id
        // if player not found then return bad response
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(target);
        if(player == null) {
            packet.respond(ResponseStatus.NOK);
            return;
        }

        // execute kick
        player.disconnect(TextComponent.fromLegacyText(ChatUtil.fabulize(message)));
        packet.respond(ResponseStatus.OK);
    }

}
