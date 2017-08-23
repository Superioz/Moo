package de.superioz.moo.cloud.listeners.player;

import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.events.MooPlayerJoinedServerEvent;

public class MooPlayerJoinedServerListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerJoinedServerEvent event) {
        //
    }

}
