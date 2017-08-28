package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.collection.UnmodifiableList;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.events.MooLoggingEvent;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.netty.client.ClientType;
import de.superioz.moo.netty.common.PacketMessenger;
import de.superioz.moo.netty.packets.PacketConsoleOutput;

/**
 * This class listens on the cloud logging, to the send it to other instances
 */
public class MooLoggingListener implements EventListener {

    @EventHandler
    public void onLogging(MooLoggingEvent event) {
        if(Cloud.getInstance() == null || Cloud.getInstance().getClientManager() == null) return;
        UnmodifiableList list = Cloud.getInstance().getClientManager().getClients(ClientType.INTERFACE);
        if(list == null || list.isEmpty()) return;

        PacketMessenger.message(new PacketConsoleOutput(event.getMessage()), ClientType.INTERFACE);
    }

}
