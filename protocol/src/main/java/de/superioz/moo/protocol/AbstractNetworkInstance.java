package de.superioz.moo.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.superioz.moo.protocol.server.MooClient;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.protocol.common.NetworkEventAdapter;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.events.PacketQueueEvent;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Getter
public abstract class AbstractNetworkInstance implements EventListener {

    /**
     * The registry of the packets accepted by this instance
     */
    private PacketRegistry registry;

    /**
     * The event handler from netty
     */
    protected EventLoopGroup eventExecutors;

    /**
     * The network bus for handling incoming and outgoing connections
     */
    private NetworkBus networkBus;

    /**
     * The netty channel of the instance
     */
    protected Channel channel;

    /**
     * The logger
     */
    private Logger logger;

    /**
     * The host of this instance
     */
    private String host;

    /**
     * The port of this instance
     */
    private int port;

    /**
     * The callback system
     */
    private final Cache<UUID, List<Consumer<AbstractPacket>>> callbacks = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES).build();

    /**
     * The network event adapters
     */
    private final List<NetworkEventAdapter> eventAdapters = new ArrayList<>();

    public AbstractNetworkInstance(String host, int port, Logger logger) {
        this.host = host;
        this.port = port;
        this.logger = logger;

        // register packets
        EventExecutor.getInstance().register(this);
        this.registry = PacketRegistry.fromProtocol();

        this.networkBus = new NetworkBus(this);
    }

    public abstract <T extends AbstractNetworkInstance> T setup();

    @EventHandler
    public void onPacketQueue(PacketQueueEvent event) {
        Channel ch = event.getChannel();
        if(ch == null) {
            ch = this.channel;
        }
        if(event.getPacket() == null) {
            return;
        }
        getNetworkBus().processOut(ch, event.getPacket(), event.getCallbacks());
    }

    /**
     * Calls an event for all adapter
     *
     * @param adapterConsumer The consumer of the adapter
     */
    public void callEvent(Consumer<NetworkEventAdapter> adapterConsumer) {
        eventAdapters.forEach(adapterConsumer);
    }

    /**
     * Registers an event adapter
     *
     * @param adapter The adapter to register
     */
    public void registerEventAdapter(NetworkEventAdapter adapter) {
        this.eventAdapters.add(adapter);
    }

    /*
    ============================================
    SENDING PACKETS
    ============================================
     */

    /**
     * Sends a packet
     *
     * @param packet    The packets
     * @param channel   The channels
     * @param consumers The consumers of the callback
     */
    public void sendPacket(AbstractPacket packet, Channel channel, Consumer<AbstractPacket>... consumers) {
        getNetworkBus().processOut(channel, packet, consumers);
    }

    public void sendPacket(AbstractPacket packet, Consumer<AbstractPacket>... consumers) {
        this.sendPacket(packet, channel, consumers);
    }

    public void sendPacket(AbstractPacket packet, List<MooClient> clients, Consumer<AbstractPacket>... callbacks) {
        PacketMessenger.create()
                .target(clients.toArray(new MooClient[clients.size()]))
                .send(packet, callbacks);
    }

}
