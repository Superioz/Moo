package de.superioz.moo.network.client;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;

/**
 * Representation of a client connected to the master server
 */
@Getter
public class MooClient {

    /**
     * The name of the connected client
     */
    private String name;

    /**
     * The host of the client (e.g.: localhost)
     */
    private String host;

    /**
     * The port of the client (e.g.: 4314) (NETTY)
     */
    private int port;

    /**
     * The subport of the client (e.g.: 25565) (SPIGOT)
     */
    private int subPort = -1;

    /**
     * The type of the client (e.g.: {@link ClientType#SERVER})
     */
    private ClientType type;

    /**
     * The netty channel of the connection
     */
    private Channel channel;

    /**
     * The id of the client
     */
    @Setter
    private int id;

    public MooClient(String name, String host, int port, int subPort, ClientType type, Channel channel) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.subPort = subPort;
        this.type = type;
        this.channel = channel;
    }

    /**
     * Returns the host and port of the client as {@link InetSocketAddress} object
     *
     * @return The object mentioned above
     */
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

}
