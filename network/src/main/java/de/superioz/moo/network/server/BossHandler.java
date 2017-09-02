package de.superioz.moo.network.server;

import de.superioz.moo.network.AbstractNetworkInstance;
import de.superioz.moo.network.packet.AbstractPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Boss handler of the netty communication
 */
public class BossHandler extends SimpleChannelInboundHandler<AbstractPacket> {

    /**
     * The handle of the netty instance
     */
    private AbstractNetworkInstance handle;

    public BossHandler(AbstractNetworkInstance handle) {
        this.handle = handle;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AbstractPacket packet) throws Exception {
        if(handle == null) return;
        try {
            this.handle.getNetworkBus().processIn(ctx.channel(), packet);
        }
        finally {
            //packet.trySingleRelease();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        handle.getLogger().warning("Netty Exception: " + cause.getMessage());
        if(cause.getMessage() == null) {
            cause.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // WHEN A CHANNEL CONNECTS TO THE SERVER
        // OR WHEN THE CLIENT CONNECTS TO THE SERVER

        // call handler event
        handle.callEvent(adapter -> adapter.onChannelActive(ctx.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // WHEN A CHANNEL DISCONNECTS FROM THE SERVER
        // OR WHEN THE CLIENT DISCONNECTS FROM THE SERVER

        // call handler event
        handle.callEvent(adapter -> adapter.onChannelInactive(ctx.channel()));
    }
}
