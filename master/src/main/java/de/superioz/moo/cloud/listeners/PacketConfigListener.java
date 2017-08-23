package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.io.MooConfigType;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketConfig;
import de.superioz.moo.protocol.packets.PacketRespond;

public class PacketConfigListener implements PacketAdapter {

    @PacketHandler
    public void onConfig(PacketConfig packet) {
        MooConfigType type = packet.type;
        String meta = packet.meta;

        // change some config
        boolean result = true;
        Object newData = meta;
        String key = type.getKey();

        // list the config key and new data
        // special types
        if(type == MooConfigType.MAINTENANCE) {
            newData = meta.equalsIgnoreCase(true + "");
        }
        else if(type == MooConfigType.MAX_PLAYERS) {
            newData = Integer.parseInt(meta);
            result = Validation.INTEGER.matches(meta);
        }
        result = result && Cloud.getInstance().getConfig().set(key, newData);

        // set to moo cache
        MooCache.getInstance().getConfigMap().putAsync(key, newData);

        // respond to the packet
        // after the con
        packet.respond(new PacketRespond(result));
        if(result) {
            // the packet config is only for trigger an event, so
            // that the redis cache doesn't need to be checked every x seconds
            PacketMessenger.message(packet.deepCopy(), ClientType.PROXY);
        }
    }

}
