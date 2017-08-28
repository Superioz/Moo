package de.superioz.moo.netty.events;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.event.Event;
import de.superioz.moo.netty.packets.PacketPlayerBan;
import lombok.Getter;

import java.net.InetSocketAddress;

@Getter
public abstract class PunishmentEvent implements Event {

    private PacketPlayerBan packet;

    private InetSocketAddress clientAddress;
    private PlayerData data;

    public PunishmentEvent(PacketPlayerBan packet, PlayerData data) {
        this.packet = packet;
        this.clientAddress = packet.getAddress();
        this.data = data;
    }

}
