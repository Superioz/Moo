package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.config.MooConfig;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.events.RedisConnectionEvent;
import de.superioz.moo.cloud.Cloud;

/**
 * This class listens on the cloud being connected to redis
 */
public class RedisConnectionListener implements EventListener {

    @EventHandler
    public void onRedisConnection(RedisConnectionEvent event) {
        if(!event.isConnectionActive()) {
            // we can ignore this, it should only happen if the cloud disables, and then the
            // network is rip either way
            return;
        }

        // if the cloud connected to redis we want to set config information into the cache
        Cloud.getInstance().setMooConfig(new MooConfig(Cloud.getInstance().getDatabaseConnection()));
        Cloud.getInstance().getMooConfig().load();
    }

}
