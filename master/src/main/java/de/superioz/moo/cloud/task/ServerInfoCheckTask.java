package de.superioz.moo.cloud.task;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.netty.client.ClientType;
import de.superioz.moo.netty.common.PacketMessenger;
import de.superioz.moo.netty.packets.PacketServerUnregister;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class ServerInfoCheckTask implements Runnable {

    private int delay;
    private int threshold;

    @Override
    public void run() {
        while(true){
            long now = System.currentTimeMillis();

            // check for last update with server
            List<UUID> toDelete = new ArrayList<>();
            for(MooServer server : Cloud.getInstance().getMooProxy().getSpigotServers().values()) {
                if(server.getLastUpdate() != -1
                        && (now - server.getLastUpdate()) > threshold) {
                    toDelete.add(server.getUuid());
                }
            }

            // delete server if they timed out and send to bungee!
            if(!toDelete.isEmpty()) {
                toDelete.forEach(uuid -> {
                    MooServer serverDeleted = Cloud.getInstance().getMooProxy().getSpigotServers().remove(uuid);
                    Cloud.getInstance().getLogger().debug("Server " + serverDeleted.getType()
                            + " [" + serverDeleted.getAddress().getHostName() + ":" + serverDeleted.getAddress().getPort() + "] timed out.");
                    PacketMessenger.message(new PacketServerUnregister(serverDeleted.getAddress()), ClientType.PROXY);

                    // sync with redis
                    MooCache.getInstance().getServerMap().removeAsync(uuid);
                });
            }

            // delay
            try {
                Thread.sleep(delay);
            }
            catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
