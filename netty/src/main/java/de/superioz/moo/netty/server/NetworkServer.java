package de.superioz.moo.netty.server;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.netty.AbstractNetworkInstance;
import de.superioz.moo.netty.Protocol;
import de.superioz.moo.netty.client.ClientType;
import de.superioz.moo.netty.client.NetworkClient;
import de.superioz.moo.netty.common.NetworkEventAdapter;
import de.superioz.moo.netty.events.MooClientDisconnectEvent;
import de.superioz.moo.netty.events.ServerStateEvent;
import de.superioz.moo.netty.packet.AbstractPacket;
import de.superioz.moo.netty.packets.PacketHandshake;
import de.superioz.moo.netty.util.PipelineUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * The network server which is used to receive connections of {@link NetworkClient}s
 */
@Getter
public class NetworkServer extends AbstractNetworkInstance {

    /*
    Netty things
     */
    private final ChannelGroup connectedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private ServerBootstrap bootstrap;
    private HostWhitelist whitelist;
    private ClientManager clientManager;

    private JsonConfig config;

    public NetworkServer(String host, int port, JsonConfig config, Logger logger) {
        super(host, port, logger);
        this.config = config;

        // list whitelist
        whitelist = new HostWhitelist(this);
        whitelist.load();

        // list hub
        clientManager = new ClientManager(this);

        // register adapter
        super.registerEventAdapter(new NetworkEventAdapter() {
            @Override
            public void onPacketReceive(AbstractPacket packet) {
            }

            @Override
            public void onHandshakeReceive(PacketHandshake handshake) {

            }

            @Override
            public void onPacketSend(AbstractPacket packet) {
                //
            }

            @Override
            public void onChannelActive(Channel channel) {
                InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();

                connectedClients.add(channel);

                // attempt means that the server isn't accepted yet
                getLogger().info("Client attempting to connect .. [@" + remoteAddress.getAddress().getHostAddress() + "]");
            }

            @Override
            public void onChannelInactive(Channel channel) {
                InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
                MooClient client = getClientManager().get(remoteAddress);
                connectedClients.remove(channel);

                getClientManager().remove(remoteAddress);

                if(client == null){
                    getLogger().warning(ConsoleColor.DARK_RED + "Client shouldn't be null at disconnecting (Address: " + remoteAddress + ")." +
                            " This can happen if the client tried to connect but wasn't able to handshake properly (Maybe he didn't have hands?)");
                }
                else{
                    getLogger().info(ConsoleColor.RED.toString()
                            + client.getType() + " client disconnected [@" + remoteAddress.getAddress().getHostAddress() + "]");
                    EventExecutor.getInstance().execute(new MooClientDisconnectEvent(client));
                }
            }
        });
    }

    /**
     * Sets the server up, that means it initialises the event group and the {@link ServerBootstrap}<br>
     * It adds different de- and encoder to the pipeline to handle packets<br>
     * <p>
     * On Unix systems Epoll is a pretty nice thing, so if this program runs on a Unix system
     * it'll use the {@link EpollEventLoopGroup} and the {@link EpollServerSocketChannel} instead of the default ones
     */
    @Override
    public NetworkServer setup() {
        this.eventExecutors = PipelineUtil.getEventLoopGroup();

        this.bootstrap = new ServerBootstrap()
                .group(eventExecutors)
                .channel(PipelineUtil.getServerChannel())
                .childHandler(PipelineUtil.getChannelInitializer(this, Protocol.PROTOCOL_VERSION));
        return this;
    }

    /**
     * Starts the server<br>
     * To inform other parts of this program, that this server is started now, it'll call a {@link ServerStateEvent}
     * per {@link EventExecutor}
     *
     * @throws Exception If something goes wrong
     */
    public void start() throws Exception {
        EventExecutor.getInstance().execute(new ServerStateEvent(this, State.STARTING));

        try {
            this.channel = bootstrap.bind(getHost(), getPort()).sync().channel();

            // calls server status event
            EventExecutor.getInstance().execute(new ServerStateEvent(this, State.STARTED));

            channel.closeFuture().sync().syncUninterruptibly();
        }
        catch(Exception e) {
            //
        }
        finally {
            if(eventExecutors != null) {
                eventExecutors.shutdownGracefully();
            }
        }
    }

    /**
     * Stops the server.<br>
     * To do so, it will close the {@link Channel} and shutdown the {@link EventLoopGroup}
     * <p>
     * It also calls the {@link ServerStateEvent} to trigger events that may need to listen to this
     */
    public void stop() {
        EventExecutor.getInstance().execute(new ServerStateEvent(this, State.STOPPING));

        channel.close();
        eventExecutors.shutdownGracefully();

        // calls server status event
        EventExecutor.getInstance().execute(new ServerStateEvent(this, State.STOPPED));
    }

    public void sendPacket(AbstractPacket packet, ClientType type, Consumer<AbstractPacket>... callbacks) {
        super.sendPacket(packet, getClientManager().getClients(type), callbacks);
    }

    /**
     * Broadcasts given packets to all connected clients
     *
     * @param packet The packet
     */
    public void broadcast(AbstractPacket packet) {
        connectedClients.forEach(channel -> sendPacket(packet, channel));
    }

    /**
     * State of the server to determine in which state the server currently is
     */
    public enum State {

        STARTING,
        STARTED,
        STOPPING,
        STOPPED

    }

}
