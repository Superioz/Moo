package de.superioz.moo.cloud.task;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.MultiPacket;
import de.superioz.moo.protocol.packets.PacketServerRegister;
import de.superioz.moo.protocol.packets.PacketServerUnregister;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class ServerInfoCheckTask implements Runnable {

    public static void main(String[] args){
        MultiPacket packet = new MultiPacket(new PacketServerRegister(), new PacketServerRegister());
        System.out.println(packet.getPacketName());
    }

    private int delay;
    private int threshold;

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(delay);
            }
            catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long now = System.currentTimeMillis();

            // check for last update with server
            List<UUID> toDelete = new ArrayList<>();
            for(MooServer server : Cloud.getInstance().getMooProxy().getSpigotServers().values()) {
                if((now - server.getLastUpdate()) > threshold) {
                    toDelete.add(server.getUuid());
                }
            }

            // delete server if they timed out and send to bungee!
            if(!toDelete.isEmpty()) {
                toDelete.forEach(uuid -> {
                    MooServer serverDeleted = Cloud.getInstance().getMooProxy().getSpigotServers().remove(uuid);
                    PacketMessenger.message(new PacketServerUnregister(serverDeleted.getAddress()));
                });
            }
        }
    }

}
