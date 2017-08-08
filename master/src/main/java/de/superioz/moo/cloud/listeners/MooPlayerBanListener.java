package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.common.punishment.BanSubType;
import de.superioz.moo.api.common.punishment.Punishmental;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.DbQueryUnbaked;
import de.superioz.moo.api.database.object.Ban;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.database.object.UniqueIdBuf;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.cloud.events.MooPlayerBanEvent;
import de.superioz.moo.cloud.events.MooPlayerPostBanEvent;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketPlayerKick;
import de.superioz.moo.protocol.packets.PacketPlayerPunish;

import java.util.List;
import java.util.UUID;

public class MooPlayerBanListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerBanEvent event) {
        PlayerData data = event.getData();
        PacketPlayerPunish packet = event.getPacket();
        List<String> meta = packet.meta;

        // get unique id buf and check if it is valid
        UniqueIdBuf buf = CloudCollections.UUID_BUFFER.get(data.lastName);
        if(buf == null) {
            packet.respond(ResponseStatus.NOK);
            return;
        }
        UUID uuid = buf.uuid;

        // checks meta (which should consist of the ban and the messages)
        // checks ban after checking the meta
        Ban ban;
        if(meta.size() != 3 || (ban = ReflectionUtil.deserialize(meta.get(0), Ban.class)) == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }
        String tempBanMessage = meta.get(1);
        String permBanMessage = meta.get(2);

        // checks if the player is banned
        Ban banBefore = CloudCollections.BAN.get(uuid);
        if(banBefore != null) {
            packet.respond(ResponseStatus.CONFLICT);
            return;
        }

        // sets ban values
        ban.start = System.currentTimeMillis();
        ban.banned = uuid;

        // checks if the executor is allowed to ban the target
        // and send FORBIDDEN if he isn't
        if(ban.by != null) {
            PlayerData executor = CloudCollections.PLAYER.get(ban.by);
            PlayerData target = CloudCollections.PLAYER.get(ban.banned);

            if(executor != null && target != null) {
                Group executorGroup = CloudCollections.GROUP.get(executor.group);
                Group targetGroup = CloudCollections.GROUP.get(target.group);

                if(!(executorGroup == null || targetGroup == null || executorGroup.rank > targetGroup.rank)) {
                    packet.respond(ResponseStatus.FORBIDDEN);
                    return;
                }
            }
        }

        // calculate the duration of the ban if the duration wasn't set before
        BanSubType reason = ban.getSubType();
        int banPoints = data.banPoints == null ? 0 : data.banPoints;
        int newBanPoints = Punishmental.calculateBanPoints(banPoints, reason);
        ban.banPoints = newBanPoints;
        if(ban.duration == 0) ban.duration = Punishmental.calculateDuration(newBanPoints);
        data.banPoints = newBanPoints;

        // ban the player and if it isn't successful then respond error
        // update the ban points of the player
        if(!CloudCollections.BAN.set(uuid, ban, true)) {
            packet.respond(ResponseStatus.NOK);
            return;
        }
        CloudCollections.PLAYER.set(data.uuid, data, DbQueryUnbaked.newInstance(DbModifier.PLAYER_BANPOINTS, data.banPoints), true);
        packet.respond(ResponseStatus.OK);

        // gets the player and kick him if he is online
        PlayerData player = Cloud.getInstance().getMooProxy().getPlayer(buf.uuid);
        if(player != null) {
            Cloud.getInstance().getMooProxy().kick(player, new PacketPlayerKick(null, buf.uuid + "",
                    ban.apply(ban.isPermanent() ? permBanMessage : tempBanMessage)), response -> {
            });
        }
        EventExecutor.getInstance().execute(new MooPlayerPostBanEvent(ban, packet, data));
    }

}
