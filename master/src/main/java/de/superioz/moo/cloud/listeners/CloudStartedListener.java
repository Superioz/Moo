package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.CloudStartedEvent;
import de.superioz.moo.protocol.common.Queries;

public class CloudStartedListener implements EventListener {

    @EventHandler
    public void onCloudStarted(CloudStartedEvent event) {
        // AND we want to put the groups into the cache :)
        try {
            Queries.list(DatabaseType.GROUP, Group.class).forEach(group -> {
                if(group == null) return;
                MooCache.getInstance().getGroupMap().fastPutAsync(group.name, group);
            });
        }
        catch(Exception e) {
            Cloud.getInstance().getLogger().debug("Couldn't load groups. It seems the database/cache is not available.");
            // MHH WEIRD
        }
    }

}
