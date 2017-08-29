package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.api.config.NetworkConfigType;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketHandler;
import de.superioz.moo.netty.packets.PacketConfig;
import de.superioz.moo.netty.packets.PacketRespond;

/**
 * This class listens on the config packet which edits something from the config
 */
public class PacketConfigListener implements PacketAdapter {

    @PacketHandler
    public void onConfig(PacketConfig packet) {
        NetworkConfigType type = packet.type;
        String meta = packet.meta;

        // set data into config
        Cloud.getInstance().getNetworkConfig().set(type, meta);

        // respond to the packet
        // after the con
        packet.respond(new PacketRespond(true));
        /*if(result) {
            // the packet config is only for trigger an event, so
            // that the redis cache doesn't need to be checked every x seconds
            PacketMessenger.message(packet.deepCopy(), ClientType.PROXY);
        }*/
    }

}
