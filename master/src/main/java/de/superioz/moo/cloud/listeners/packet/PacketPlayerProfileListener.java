package de.superioz.moo.cloud.listeners.packet;

import com.mongodb.client.model.Filters;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.util.SimpleSerializable;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketPlayerProfile;
import de.superioz.moo.protocol.packets.PacketRespond;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class listens on the PacketPlayerProfile, a class to get all ban and data information
 * of one player
 */
public class PacketPlayerProfileListener implements PacketAdapter {

    @PacketHandler
    public void onPlayerInfo(PacketPlayerProfile packet) throws Exception {
        String id = packet.id;
        UUID uuid = null;

        // ..
        ResponseStatus status = ResponseStatus.OK;

        // if the id is a uuid, perfect use it
        // if not, then find the corresponding uuid for the players name
        if(Validation.UNIQUEID.matches(id)) {
            uuid = UUID.fromString(id);
        }

        // if the uuid couldn't be found or the playerData is (therefore) empty
        // just return a bad status
        PlayerData playerData = DatabaseCollections.PLAYER.list(
                new DbFilter(uuid instanceof UUID
                        ? Filters.eq(DbModifier.PLAYER_UUID.getFieldName(), uuid)
                        : Filters.eq(DbModifier.PLAYER_NAME.getFieldName(), id))).get(0);
        if(status != ResponseStatus.OK || playerData == null) {
            packet.respond(status);
            return;
        }

        // respond which is a list of the data
        // 0 = playerData, 1 = currentBan, 2 = archivedBans
        List<String> respond = new ArrayList<>();

        // 0; get the playerData
        respond.add(playerData.toString());

        // 1; list the current ban
        Ban ban = DatabaseCollections.BAN.get(uuid);
        respond.add(ban != null ? ban.toString() : "");

        // 2; list former bans
        List<Ban> archivedBans = DatabaseCollections.BAN_ARCHIVE.list(DbFilter.fromPrimKey(Ban.class, uuid));
        respond.add((archivedBans != null && !archivedBans.isEmpty())
                ? StringUtil.getListToString(archivedBans, StringUtil.SEPERATOR_2, SimpleSerializable::toString)
                : "");

        // send respond
        packet.respond(new PacketRespond(packet.getName().toLowerCase(), respond, status));
    }

}
