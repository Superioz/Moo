package de.superioz.moo.client;

import io.netty.channel.Channel;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.client.events.ClientConnectedEvent;
import de.superioz.moo.protocol.common.NetworkEventAdapter;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.common.Response;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packets.PacketHandshake;

import java.util.function.Consumer;

public class MooNetworkAdapter implements NetworkEventAdapter {

    private Moo moo;

    public MooNetworkAdapter(Moo moo) {
        this.moo = moo;
    }

    @Override
    public void onPacketReceive(AbstractPacket packet) {

    }

    @Override
    public void onHandshakeReceive(PacketHandshake handshake) {

    }

    @Override
    public void onPacketSend(AbstractPacket packet) {

    }

    @Override
    public void onChannelActive(Channel channel) {
        moo.getLogger().info("Request connection to cloud ..");

        PacketMessenger.transferToResponse(new PacketHandshake(moo.getClientName(), moo.getClientType()),
                (Consumer<Response>) response -> {
                    EventExecutor.getInstance().execute(new ClientConnectedEvent(response.getStatus()));

                    /*// made every plugin prepare
                    Moo.getInstance().getPluginManager().onStateChange();*/

                    // set authenticated
                    moo.getClient().setAuthenticated(response.isOk());
                    moo.getClient().setMasterVersion(response.getMessage());
                });
    }

    @Override
    public void onChannelInactive(Channel channel) {
        moo.getLogger().info("Disconnected from cloud.");

        /*// made every plugin prepare
        Moo.getInstance().getPluginManager().onStateChange();*/

        if(!moo.isAutoReconnect()) return;
        try {
            moo.getClient().attemptReconnecting(moo.getExecutors());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
