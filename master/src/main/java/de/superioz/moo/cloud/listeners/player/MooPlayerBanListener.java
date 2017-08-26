package de.superioz.moo.cloud.listeners.player;

import com.mongodb.client.model.Filters;
import de.superioz.moo.api.common.punishment.BanCategory;
import de.superioz.moo.api.common.punishment.PunishmentManager;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.query.DbQueryUnbaked;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.cloud.events.MooPlayerBanEvent;
import de.superioz.moo.cloud.events.MooPlayerPostBanEvent;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.exception.MooInputException;
import de.superioz.moo.protocol.packets.PacketPlayerBan;
import de.superioz.moo.protocol.packets.PacketPlayerKick;

import java.util.UUID;

/**
 * This class listens on the command to ban a player
 */
public class MooPlayerBanListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerBanEvent event) throws MooInputException {
        PlayerData data = event.getData();
        PacketPlayerBan packet = event.getPacket();
        Ban ban = packet.ban;

        // list unique id buf and check if it is valid
        PlayerData fetchedData = DatabaseCollections.PLAYER.get(new DbFilter(Filters.eq(DbModifier.PLAYER_NAME.getFieldName(), data.lastName)));
        UUID uuid = fetchedData.uuid;

        // checks ban
        if(ban == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }
        String tempBanMessage = packet.banTempMessage;
        String permBanMessage = packet.banPermMessage;

        // checks if the player is banned
        Ban banBefore = DatabaseCollections.BAN.get(uuid);
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
            PlayerData executor = DatabaseCollections.PLAYER.get(ban.by);
            PlayerData target = DatabaseCollections.PLAYER.get(ban.banned);

            if(executor != null && target != null) {
                Group executorGroup = DatabaseCollections.GROUP.get(executor.group);
                Group targetGroup = DatabaseCollections.GROUP.get(target.group);

                if(!(executorGroup == null || targetGroup == null || executorGroup.rank > targetGroup.rank)) {
                    packet.respond(ResponseStatus.FORBIDDEN);
                    return;
                }
            }
        }

        // calculate the duration of the ban if the duration wasn't set before
        BanCategory reason = ban.getSubType();
        int banPoints = data.banPoints == null ? 0 : data.banPoints;
        int newBanPoints = PunishmentManager.calculateBanPoints(banPoints, reason);
        ban.banPoints = newBanPoints;
        if(ban.duration == 0) ban.duration = PunishmentManager.calculateDuration(newBanPoints);
        data.banPoints = newBanPoints;

        // ban the player and if it isn't successful then respond error
        // update the ban points of the player
        if(!DatabaseCollections.BAN.set(uuid, ban)) {
            packet.respond(ResponseStatus.NOK);
            return;
        }
        DatabaseCollections.PLAYER.set(data.uuid, data, DbQueryUnbaked.newInstance(DbModifier.PLAYER_BANPOINTS, data.banPoints));
        packet.respond(ResponseStatus.OK);

        // gets the player and kick him if he is online
        PlayerData player = Cloud.getInstance().getMooProxy().getPlayer(fetchedData.uuid);
        if(player != null) {
            Cloud.getInstance().getMooProxy().kick(player, new PacketPlayerKick(null, fetchedData.uuid + "",
                    ban.apply(ban.isPermanent() ? permBanMessage : tempBanMessage)), response -> {
            });
        }
        EventExecutor.getInstance().execute(new MooPlayerPostBanEvent(ban, packet, data));
    }

}