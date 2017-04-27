package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.common.MooPlayer;
import de.superioz.moo.api.database.object.Ban;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.database.object.UniqueIdBuf;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.cloud.events.MooPlayerMuteEvent;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketPlayerPunish;

import java.util.List;
import java.util.UUID;

public class MooPlayerMuteListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerMuteEvent event) {
        PlayerData data = event.getData();
        PacketPlayerPunish packet = event.getPacket();
        List<String> meta = packet.meta;

        // get unique id buf and check if it is valid
        UniqueIdBuf buf = CloudCollections.uniqueIds().get(data.lastName);
        if(buf == null) {
            packet.respond(ResponseStatus.NOK);
            return;
        }
        UUID uuid = buf.uuid;

        // checks meta (which should consist of the mute/ban and the messages)
        // checks ban after checking the meta
        Ban ban;
        if(meta.size() != 2 || (ban = ReflectionUtil.deserialize(meta.get(0), Ban.class)) == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }
        String message = meta.get(1);

        // checks if the player is muted
        Ban banBefore = CloudCollections.mutes().get(uuid);
        if(banBefore != null) {
            packet.respond(ResponseStatus.CONFLICT);
            return;
        }

        // checks if the executor is allowed to mute the target
        // and send FORBIDDEN if he isn't
        if(ban.by != null) {
            PlayerData executor = CloudCollections.players().get(ban.by);
            PlayerData target = CloudCollections.players().get(ban.banned);

            if(executor != null && target != null) {
                Group executorGroup = CloudCollections.groups().get(executor.group);
                Group targetGroup = CloudCollections.groups().get(target.group);

                if(!(executorGroup == null || targetGroup == null || executorGroup.rank > targetGroup.rank)) {
                    packet.respond(ResponseStatus.FORBIDDEN);
                    return;
                }
            }
        }

        // ban the player and if it isn't successful then respond error
        // update the ban points of the player
        if(!CloudCollections.mutes().set(uuid, ban, true)) {
            packet.respond(ResponseStatus.NOK);
            return;
        }

        // gets the player and send him message
        MooPlayer player = Cloud.getInstance().getMooProxy().getPlayer(buf.uuid);
        if(player != null) {
            Cloud.getInstance().getMooProxy().sendMessage(player, message);
        }
    }

}
