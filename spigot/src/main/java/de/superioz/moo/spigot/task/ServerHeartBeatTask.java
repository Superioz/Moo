package de.superioz.moo.spigot.task;

import de.superioz.moo.netty.common.PacketMessenger;
import de.superioz.moo.netty.packets.PacketServerHeartBeat;
import de.superioz.moo.spigot.Lightning;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.InetSocketAddress;

@AllArgsConstructor
public class ServerHeartBeatTask implements Runnable {

    @Getter
    private int delay;

    @Override
    public void run() {
        while(true){
            // send heartBeat to the cloud
            PacketMessenger.message(new PacketServerHeartBeat(
                    new InetSocketAddress(Lightning.getInstance().getServer().getIp(), Lightning.getInstance().getServer().getPort())
            ));

            // delay
            try {
                Thread.sleep(delay);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
