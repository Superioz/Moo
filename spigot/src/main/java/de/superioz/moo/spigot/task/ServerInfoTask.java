package de.superioz.moo.spigot.task;

import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.PacketServerInfoUpdate;
import de.superioz.moo.spigot.Lightning;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
@Getter
public class ServerInfoTask implements Runnable {

    private int delay;

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(delay);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }

            // get server info
            Server server = Lightning.getInstance().getServer();
            String motd = server.getMotd();
            List<String> players = new ArrayList<>();
            server.getOnlinePlayers().forEach((Consumer<Player>) player -> players.add(player.getName() + ":" + player.getUniqueId()));
            int maxPlayers = server.getMaxPlayers();

            // send serverInfo to the cloud
            PacketMessenger.message(new PacketServerInfoUpdate(
                    Lightning.getInstance().getUuid(),
                    Lightning.getInstance().getType(),
                    motd, players.size(), maxPlayers, players)
            );
        }
    }
}
