package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.objects.UniqueIdBuf;
import de.superioz.moo.api.util.SimpleSerializable;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketPlayerInfo;
import de.superioz.moo.protocol.packets.PacketRespond;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketPlayerInfoListener implements PacketAdapter {

    @PacketHandler
    public void onPlayerInfo(PacketPlayerInfo packet) throws Exception {
        String id = packet.id;
        UUID uuid = null;

        // ..
        ResponseStatus status = ResponseStatus.OK;

        // if the id is a uuid, perfect use it
        // if not, then find the corresponding uuid for the players name
        if(Validation.UNIQUEID.matches(id)) {
            uuid = UUID.fromString(id);
        }
        else {
            UniqueIdBuf buf = CloudCollections.UUID_BUFFER.get(id);
            if(buf == null) {
                status = ResponseStatus.NOT_FOUND;
            }
            else {
                uuid = buf.uuid;
            }
        }

        // if the uuid couldn't be found or the playerData is (therefore) empty
        // just return a bad status
        PlayerData playerData;
        if(status != ResponseStatus.OK
                || (playerData = CloudCollections.PLAYER.get(uuid)) == null) {
            packet.respond(status);
            return;
        }

        // respond
        List<String> respond = new ArrayList<>();
        respond.add(playerData.toString());

        // get the current ban
        Ban ban = CloudCollections.BAN.get(uuid);
        respond.add(ban != null ? ban.toString() : "");

        // get former bans
        List<Ban> archivedBans =
                CloudCollections.BAN_ARCHIVE.get(DbFilter.fromPrimKey(Ban.class, uuid), false, -1);
        respond.add((archivedBans != null && !archivedBans.isEmpty())
                ? StringUtil.getListToString(archivedBans, StringUtil.SEPERATOR_2, SimpleSerializable::toString)
                : "");

        // send respond
        packet.respond(new PacketRespond(packet.getName().toLowerCase(), respond, status));
    }

}
