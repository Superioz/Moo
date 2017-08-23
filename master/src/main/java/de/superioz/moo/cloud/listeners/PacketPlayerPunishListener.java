package de.superioz.moo.cloud.listeners;

import com.mongodb.client.model.Filters;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.reaction.Reactor;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.cloud.events.MooPlayerBanEvent;
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

        // list the playerData
        PlayerData data = Validation.UNIQUEID.matches(target)
                ? DatabaseCollections.PLAYER.get(UUID.fromString(target))
                : DatabaseCollections.PLAYER.list(new DbFilter(Filters.eq(DbModifier.PLAYER_NAME.getFieldName(), target))).get(0);

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
