package de.superioz.moo.cloud.task;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.cloud.Cloud;
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
            try {
                Thread.sleep(delay);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }

            long now = System.currentTimeMillis();

            // check for last update with server
            List<UUID> toDelete = new ArrayList<>();
            for(MooServer server : Cloud.getInstance().getMooProxy().getSpigotServer().values()) {
                if((now - server.getLastUpdate()) > threshold) {
                    toDelete.add(server.getUuid());
                }
            }

            // delete server if they timed out
            if(!toDelete.isEmpty()) {
                toDelete.forEach(uuid -> Cloud.getInstance().getMooProxy().getSpigotServer().remove(uuid));
            }
        }
    }

}
