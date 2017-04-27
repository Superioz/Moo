package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.exceptions.InvalidConfigException;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.MultiPacket;
import de.superioz.moo.protocol.packets.PacketConfig;
import de.superioz.moo.protocol.packets.PacketRespond;

import java.util.ArrayList;
import java.util.List;

public class PacketConfigListener implements PacketAdapter {

    @PacketHandler
    public void onConfig(PacketConfig packet) {
        PacketConfig.Type type = packet.type;
        PacketConfig.Command command = packet.command;
        String meta = packet.meta;

        // change some config
        boolean changeConfig = command == PacketConfig.Command.CHANGE;
        Reaction.react(changeConfig, () -> {
            boolean result = true;
            Object newData = meta;
            String key = type.getKey();

            // get the config key and new data
            // special types
            if(type == PacketConfig.Type.MAINTENANCE) {
                newData = meta.equalsIgnoreCase("true");
            }
            else if(type == PacketConfig.Type.MAX_PLAYERS) {
                newData = Integer.parseInt(meta);
                result = Validation.INTEGER.matches(meta);
            }
            result = result && Cloud.getInstance().getConfig().set(key, newData);

            // respond to the packet
            // after the con
            packet.respond(new PacketRespond(result));
            if(result) {
                PacketMessenger.message(packet.deepCopy(), ClientType.PROXY);
            }
        });

        // get some config
        Reaction.react(!changeConfig, () -> {
            // loop through keys for fetching data
            List<PacketConfig> data = new ArrayList<>();
            type.getKeys().forEach(s -> {
                try {
                    Object o = Cloud.getInstance().getConfig().get(s.getKey());
                    data.add(new PacketConfig(PacketConfig.Command.CHANGE, s, o + ""));
                }
                catch(InvalidConfigException ex) {
                    //
                }
            });

            MultiPacket multiPacket = new MultiPacket<>(data);
            packet.respond(multiPacket);
        });
    }

}
