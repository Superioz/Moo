package de.superioz.moo.proxy.listeners;

import de.superioz.moo.client.common.ProxyCache;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketRespond;

public class PacketRespondListener implements PacketAdapter {

    @PacketHandler
    public void onRespond(PacketRespond respond) {
        if(respond == null || respond.status != ResponseStatus.OK) return;
        String header = respond.header;

        // if the header is not the modification prefix
        if(!header.startsWith(PacketRespond.MODIFICATION_PREFIX)) {
            return;
        }

        // get the type of the modification (player, group, ..)
        // loop through respond message (different updates)
        String type = header.replaceFirst(PacketRespond.MODIFICATION_PREFIX, "");
        for(String s : respond.message) {
            // get the modification type (create, modify, delete, ..)
            String[] s0 = s.split(":", 2);
            int mod = Integer.parseInt(s0[0]);

            // apply the modification to the cache
            ProxyCache.getInstance().update(type, s0[1], mod);
        }
    }

}
