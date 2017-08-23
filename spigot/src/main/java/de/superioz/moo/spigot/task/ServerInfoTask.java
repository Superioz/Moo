package de.superioz.moo.spigot.task;

import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.PacketServerInfoUpdate;
import de.superioz.moo.spigot.Lightning;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class ServerInfoTask implements Runnable {

    @Getter
    private int delay;

    @Override
    public void run() {
        while(true){
            // list server info
            Server server = Lightning.getInstance().getServer();
            String motd = server.getMotd();
            List<String> players = new ArrayList<>();
            server.getOnlinePlayers().forEach((Consumer<Player>) player -> players.add(player.getName() + ":" + player.getUniqueId()));
            int maxPlayers = server.getMaxPlayers();

            // send serverInfo to the cloud
            PacketMessenger.message(new PacketServerInfoUpdate(
                    new InetSocketAddress(Lightning.getInstance().getServer().getIp(), Lightning.getInstance().getServer().getPort()),
                    motd, players.size(), maxPlayers)
            );

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
