package de.superioz.moo.cloud.listeners.packet;

import com.mongodb.client.model.Filters;
import de.superioz.moo.api.common.punishment.BanCategory;
import de.superioz.moo.api.common.punishment.PunishmentManager;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.query.DbQueryUnbaked;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.network.common.ResponseStatus;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketPlayerBan;
import de.superioz.moo.network.packets.PacketPlayerKick;

import java.util.UUID;

/**
 * This class listens to the PacketPlayerBan
 */
public class PacketPlayerBanListener implements PacketAdapter {

    @PacketHandler
    public void onPlayerBan(PacketPlayerBan packet) {
        String target = packet.target;

        // ban is not null
        Ban ban = packet.ban;
        if(ban == null){
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        // get the playerData
        // check if the data is null
        PlayerData data = Validation.UNIQUEID.matches(target)
                ? DatabaseCollections.PLAYER.get(UUID.fromString(target))
                : DatabaseCollections.PLAYER.get(new DbFilter(Filters.eq(DbModifier.PLAYER_NAME.getFieldName(), target)));
        if(data == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // sets ban values
        ban.setStart(System.currentTimeMillis());
        ban.setBanned(data.getUuid());

        // calculate the duration of the ban if the duration wasn't set before
        BanCategory reason = ban.getSubType();
        int banPoints = data.getBanPoints() == null ? 0 : data.getBanPoints();
        int newBanPoints = PunishmentManager.calculateBanPoints(banPoints, reason);
        ban.setBanPoints(newBanPoints);
        if(ban.getDuration() == 0) ban.setDuration(PunishmentManager.calculateDuration(newBanPoints));
        data.setBanPoints(newBanPoints);

        // ban the player and if it isn't successful then respond error
        // update the ban points of the player
        if(!DatabaseCollections.BAN.set(data.getUuid(), ban)) {
            packet.respond(ResponseStatus.NOK);
            return;
        }
        DatabaseCollections.PLAYER.set(data.getUuid(), data, DbQueryUnbaked.newInstance(DbModifier.PLAYER_BANPOINTS, data.getBanPoints()));
        packet.respond(ResponseStatus.OK);

        // gets the player and kick him if he is online
        String tempBanMessage = packet.banTempMessage;
        String permBanMessage = packet.banPermMessage;
        PlayerData player = Cloud.getInstance().getNetworkProxy().getPlayer(data.getUuid());
        if(player != null) {
            Cloud.getInstance().getNetworkProxy().kick(player, new PacketPlayerKick(null, data.getUuid(),
                    ban.apply(ban.isPermanent() ? permBanMessage : tempBanMessage)), response -> {
            });
        }
    }

}
