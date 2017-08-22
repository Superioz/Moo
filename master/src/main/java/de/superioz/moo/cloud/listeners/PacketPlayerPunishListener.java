package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.objects.UniqueIdBuf;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.reaction.Reactor;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.events.MooPlayerBanEvent;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.cloud.events.MooPlayerKickEvent;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketPlayerPunish;

import java.util.UUID;

public class PacketPlayerPunishListener implements PacketAdapter {

    @PacketHandler
    public void onPlayerPunish(PacketPlayerPunish packet) {
        String target = packet.target;

        // get the playerData
        PlayerData data = null;
        if(Validation.UNIQUEID.matches(target)) {
            data = CloudCollections.PLAYER.get(UUID.fromString(target));
        }
        else {
            UniqueIdBuf buf = CloudCollections.UUID_BUFFER.get(target);

            if(buf != null) {
                data = CloudCollections.PLAYER.get(buf.uuid);
            }
        }

        // if the data is null
        if(data == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // fire specific event
        PlayerData finalData = data;
        Reaction.react(packet.type,
                new Reactor<PacketPlayerPunish.Type>(PacketPlayerPunish.Type.BAN) {
                    @Override
                    public void invoke() {
                        // the player needs to be banned
                        EventExecutor.getInstance().execute(new MooPlayerBanEvent(packet, finalData));
                    }
                }, new Reactor<PacketPlayerPunish.Type>(PacketPlayerPunish.Type.KICK) {
                    @Override
                    public void invoke() {
                        // the player needs to be kicked
                        EventExecutor.getInstance().execute(new MooPlayerKickEvent(packet, finalData));
                    }
                });
    }

}
