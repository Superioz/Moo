package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.events.RedisConnectionEvent;
import de.superioz.moo.api.io.MooConfigType;
import de.superioz.moo.cloud.Cloud;

public class RedisConnectionListener implements EventListener {

    @EventHandler
    public void onRedisConnection(RedisConnectionEvent event) {
        if(!event.isConnectionActive()) {
            // we can ignore this, it should only happen if the cloud disables, and then the
            // network is rip either way
            return;
        }

        // if the cloud connected to redis we want to set config information into the cache
        for(MooConfigType configType : MooConfigType.values()) {
            MooCache.getInstance().getConfigMap().fastPutAsync(configType.getKey(),
                    Cloud.getInstance().getConfig().get("minecraft." + configType.getKey()));
        }
    }

}
