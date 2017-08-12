package de.superioz.moo.cloud.modules;

import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.api.module.ModuleDependency;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.CloudStartedEvent;
import de.superioz.moo.cloud.events.HandshakeEvent;
import de.superioz.moo.protocol.events.MooClientConnectedEvent;
import de.superioz.moo.cloud.listeners.HandshakeListener;
import de.superioz.moo.protocol.common.NetworkEventAdapter;
import de.superioz.moo.protocol.events.ServerStateEvent;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packets.PacketHandshake;
import de.superioz.moo.protocol.server.MooClient;
import de.superioz.moo.protocol.server.NetworkServer;
import io.netty.channel.Channel;
import lombok.Getter;

import java.net.InetSocketAddress;

@ModuleDependency(modules = {"config"})
@RunAsynchronous
@Getter
public class NettyModule extends Module implements EventListener {

    private NetworkServer server;
    private JsonConfig config;

    public NettyModule(JsonConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return "netty";
    }

    @Override
    protected void onEnable() {
        Cloud.getInstance().getLogger().info("Starting netty server ..");
        this.server = new NetworkServer(config.get("netty.host"), config.get("netty.port"), config, Cloud.getInstance().getLogger().getBaseLogger());

        // register protocol listeners
        //EventExecutor.getInstance().register(new NettyServerListener(server));
        EventExecutor.getInstance().register(this);
        EventExecutor.getInstance().register(new HandshakeListener());

        // register event adapter
        server.registerEventAdapter(new NetworkEventAdapter() {
            @Override
            public void onPacketReceive(AbstractPacket packet) {
            }

            @Override
            public void onHandshakeReceive(PacketHandshake handshake) {
                EventExecutor.getInstance().execute(new HandshakeEvent(handshake.getChannel(), handshake));
            }

            @Override
            public void onPacketSend(AbstractPacket packet) {
            }

            @Override
            public void onChannelActive(Channel channel) {
            }

            @Override
            public void onChannelInactive(Channel channel) {
                MooClient client = Cloud.getInstance().getClientManager().get((InetSocketAddress)channel.remoteAddress());
                if(client == null) return;

                EventExecutor.getInstance().execute(new MooClientConnectedEvent(client));
            }
        });

        // send info to program parts waiting for me
        super.finished(true);
        EventExecutor.getInstance().execute(new CloudStartedEvent());

        try {
            server.setup().start();
        }
        catch(Exception e) {
            Cloud.getInstance().getLogger().severe("Couldn't start netty server!", e);
        }
    }

    @Override
    protected void onDisable() {
        server.stop();
    }

    @EventHandler
    public void onServerState(ServerStateEvent stateEvent) {
        if(stateEvent.getState() == NetworkServer.State.STARTED) {
            Cloud.getInstance().getLogger().info(ConsoleColor.WHITE
                    + "Netty master started [" + server.getHost() + ":" + server.getPort() + "]");
        }
    }

}
