package de.superioz.moo.netty.client;

import de.superioz.moo.netty.server.NetworkServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import de.superioz.moo.netty.AbstractNetworkInstance;
import de.superioz.moo.netty.util.PipelineUtil;
import de.superioz.moo.netty.Protocol;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * In a netty network this is the client which connects to the {@link NetworkServer}
 */
@Getter
public class NetworkClient extends AbstractNetworkInstance {

    /**
     * How often should the client try to reconnect before giving up?
     */
    public static final int RECONNECT_TIMES = 5;

    /**
     * Time between every reconnect attempt in millis
     */
    public static final int RECONNECT_DELAY = 3 * 1000;

    /**
     * The netty bootstrap instance
     */
    private Bootstrap bootstrap;

    /**
     * Value if the client is authenticated
     */
    @Setter
    private boolean authenticated = false;

    /**
     * Version of the cloud (will be fetched after connected)
     */
    @Setter
    private String masterVersion;

    public NetworkClient(String host, int port, Logger logger) {
        super(host, port, logger);
    }

    /**
     * Sets the client up, that means initializing the network instance
     *
     * @see PipelineUtil
     * @see Bootstrap
     */
    @Override
    public NetworkClient setup() {
        this.eventExecutors = PipelineUtil.getEventLoopGroup();

        this.bootstrap = new Bootstrap()
                .group(eventExecutors)
                .channel(PipelineUtil.getChannel())
                .handler(PipelineUtil.getChannelInitializer(this, Protocol.PROTOCOL_VERSION));
        return this;
    }

    /**
     * Checks if the channel is connected (and so active)
     *
     * @return The result
     */
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    /**
     * Starts the client that means starting the {@link NioEventLoopGroup}
     * and connecting the {@link #bootstrap}
     *
     * @throws Exception If something goes wrong
     */
    public void connect() throws Exception {
        EventLoopGroup eventExecutors = new NioEventLoopGroup();

        try {
            this.channel = bootstrap.connect(getHost(), getPort()).sync().channel();

            channel.closeFuture().sync().syncUninterruptibly();
        }
        finally {
            eventExecutors.shutdownGracefully();
        }
    }

    /**
     * Disconnects the channel ({@link #channel})
     *
     * @see io.netty.channel.Channel
     */
    public void disconnect() {
        channel.close();
        channel.disconnect();
    }

    /**
     * Tries to reconnect to the server it was connected to
     *
     * @param executor The executor service to run it async
     */
    public boolean attemptReconnecting(ExecutorService executor) {
        if(isConnected()) return false;

        getLogger().info("Trying to reconnect to cloud ..");
        executor.execute(() -> {
            int failedCount = 0;

            while(true){
                if(failedCount == RECONNECT_TIMES) {
                    getLogger().info("Failed " + failedCount + " times to reconnect. Giving up :(");
                    return;
                }

                // wait
                try {
                    Thread.sleep(RECONNECT_DELAY);
                }
                catch(InterruptedException e1) {
                    e1.printStackTrace();
                }

                try {
                    connect();
                }
                catch(Exception e1) {
                    failedCount++;
                    continue;
                }
                getLogger().info("Successfully reconnected!");
            }
        });
        return true;
    }

}
