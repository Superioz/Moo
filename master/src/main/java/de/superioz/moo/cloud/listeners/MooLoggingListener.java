package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.events.MooLoggingEvent;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.PacketConsoleOutput;

import java.util.logging.LogRecord;

public class MooLoggingListener implements EventListener {

    @EventHandler
    public void onLogging(MooLoggingEvent event) {
        LogRecord record = event.getRecord();

        PacketMessenger.message(new PacketConsoleOutput(record.getMessage().trim()), ClientType.INTERFACE);
    }

}
