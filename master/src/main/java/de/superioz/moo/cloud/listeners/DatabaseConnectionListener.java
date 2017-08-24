package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.config.MooConfig;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.DatabaseConnectionEvent;

/**
 * Listens to the database being connected/disconnected
 */
public class DatabaseConnectionListener implements EventListener {

    @EventHandler
    public void onDatabaseConnection(DatabaseConnectionEvent event) {
        // ..

        if(event.isConnectionActive()) {
            // if the cloud connected to database we want to set config information into the cache
            Cloud.getInstance().setMooConfig(new MooConfig(event.getConnection()));
            Cloud.getInstance().getMooConfig().load();
        }
    }

}
