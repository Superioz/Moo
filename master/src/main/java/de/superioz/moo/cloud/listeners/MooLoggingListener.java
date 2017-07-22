package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.collection.UnmodifiableList;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.events.MooLoggingEvent;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.PacketConsoleOutput;

public class MooLoggingListener implements EventListener {

    @EventHandler
    public void onLogging(MooLoggingEvent event) {
        if(Cloud.getInstance() == null || Cloud.getInstance().getHub() == null) return;
        UnmodifiableList list = Cloud.getInstance().getHub().getClients(ClientType.INTERFACE);
        if(list == null || list.isEmpty()) return;

        PacketMessenger.message(new PacketConsoleOutput(event.getMessage()), ClientType.INTERFACE);
    }

}
