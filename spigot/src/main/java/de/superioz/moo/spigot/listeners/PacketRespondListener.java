package de.superioz.moo.spigot.listeners;

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
        if(!header.startsWith("mod-")) return;

        // ..
        String type = header.replaceFirst("mod-", "");
        for(String s : respond.message) {

            String[] s0 = s.split(":", 2);
            int mod = Integer.parseInt(s0[0]);
            ProxyCache.getInstance().update(type, s0[1], mod);
        }
    }

}
