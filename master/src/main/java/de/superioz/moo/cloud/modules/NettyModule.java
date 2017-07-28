package de.superioz.moo.cloud.modules;

import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.cloud.listeners.HandshakeListener;
import io.netty.channel.Channel;
import lombok.Getter;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.api.module.ModuleDependency;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.HandshakeEvent;
import de.superioz.moo.protocol.common.NetworkEventAdapter;
import de.superioz.moo.protocol.events.ServerStateEvent;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packets.PacketHandshake;
import de.superioz.moo.protocol.server.NetworkServer;

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
        Cloud.getLogger().info("Starting netty server ..");
        this.server = new NetworkServer(config.get("netty.host"), config.get("netty.port"), config, Cloud.getLogger().getLogger());

        // register protocol listeners
        //EventExecutor.getInstance().register(new NettyServerListener(server));
        EventExecutor.getInstance().register(this);
        EventExecutor.getInstance().register(new HandshakeListener());

        server.registerEventAdapter(new NetworkEventAdapter() {
            @Override
            public void onPacketReceive(AbstractPacket packet) {
                //
            }

            @Override
            public void onHandshakeReceive(PacketHandshake handshake) {
                EventExecutor.getInstance().execute(new HandshakeEvent(handshake.getChannel(), handshake));
            }

            @Override
            public void onPacketSend(AbstractPacket packet) {
                //
            }

            @Override
            public void onChannelActive(Channel channel) {
                //
            }

            @Override
            public void onChannelInactive(Channel channel) {
                //
            }
        });

        //
        super.finished(true);

        try {
            server.setup().start();
        }
        catch(Exception e) {
            Cloud.getLogger().severe("Couldn't start netty server!", e);
        }
    }

    @Override
    protected void onDisable() {
        server.stop();
    }

    @EventHandler
    public void onServerState(ServerStateEvent stateEvent) {
        if(stateEvent.getState() == NetworkServer.State.STARTED) {
            Cloud.getLogger().info(ConsoleColor.LIGHT_PURPLE
                    + "Netty master started [" + server.getHost() + ":" + server.getPort() + "]");
        }
    }

}
