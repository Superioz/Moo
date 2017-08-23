package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.reaction.Reactor;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.cloud.events.MooPlayerConnectedServerEvent;
import de.superioz.moo.cloud.events.MooPlayerJoinedProxyEvent;
import de.superioz.moo.cloud.events.MooPlayerJoinedServerEvent;
import de.superioz.moo.cloud.events.MooPlayerLeftProxyEvent;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketPlayerState;

public class PacketPlayerStateListener implements PacketAdapter {

    @PacketHandler
    public void onStateChange(PacketPlayerState packet) {
        PlayerData playerData = packet.data;
        PacketPlayerState.State newState = packet.state;

        // list current data from player
        // check if the data is valid
        // otherwise update the data of the packets for the events
        PlayerData currentData = DatabaseCollections.PLAYER.getCurrentData(playerData, true);
        if(currentData == null) {
            packet.respond(ResponseStatus.NOK);
            return;
        }

        // check state and eventually validates uuid buf
        if(newState == PacketPlayerState.State.JOIN_PROXY
                || newState == PacketPlayerState.State.JOIN_SERVER) {
            //
        }
        packet.data = currentData;

        // reacts to the state of the player
        Reaction.react(newState
                // the player joins the proxy (bungee)
                , new Reactor<PacketPlayerState.State>(PacketPlayerState.State.JOIN_PROXY) {
                    @Override
                    public void invoke() {
                        // 1. sets the time when the player joined
                        // 2. check maintenance status
                        // 3. sets the server/proxy he is now online (meta=serverId)(hub#address=proxyId)
                        // 4. change the user count

                        // awaits playerdata and group as respond (ProxyCache)
                        EventExecutor.getInstance().execute(new MooPlayerJoinedProxyEvent(packet));
                    }
                }
                // the player leaves the proxy (bungee)
                , new Reactor<PacketPlayerState.State>(PacketPlayerState.State.LEAVE_PROXY) {
                    @Override
                    public void invoke() {
                        // 1. calculates the time he was online
                        // 2. removes the server he was online on
                        // 3. change the user count
                        EventExecutor.getInstance().execute(new MooPlayerLeftProxyEvent(packet));
                    }
                }
                // the player joins a server (spigot)
                , new Reactor<PacketPlayerState.State>(PacketPlayerState.State.JOIN_SERVER) {
                    @Override
                    public void invoke() {
                        // awaits playerdata and group as respond (ProxyCache)
                        EventExecutor.getInstance().execute(new MooPlayerJoinedServerEvent(packet));
                    }
                }
                // the player connects to a server (bungee)
                , new Reactor<PacketPlayerState.State>(PacketPlayerState.State.CONNECT_SERVER) {
                    @Override
                    public void invoke() {
                        // only set new current server to player
                        EventExecutor.getInstance().execute(new MooPlayerConnectedServerEvent(packet));
                    }
                }
        );
        if(packet.isResponded()) return;

        // send respond
        packet.respond(ResponseStatus.fromCondition(currentData != null));
    }

}
