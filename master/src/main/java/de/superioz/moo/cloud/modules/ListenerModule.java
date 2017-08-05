package de.superioz.moo.cloud.modules;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.listeners.*;
import de.superioz.moo.protocol.packet.PacketAdapting;
import lombok.Getter;

@Getter
public class ListenerModule extends Module {

    @Override
    public String getName() {
        return "listener";
    }

    @Override
    protected void onEnable() {
        Cloud.getInstance().getLogger().debug("Registering listeners ..");

        // event listeners
        EventExecutor.getInstance().register(
                new QueryServerListener(),
                new MooClientConnectedListener(),
                new MooPlayerJoinedProxyListener(),
                new MooPlayerLeftProxyListener(),
                new MooPlayerBanListener(),
                new MooPlayerPostBanListener(),
                new MooPlayerKickListener(),
                new MooPlayerConnectedServerListener(),
                new MooPlayerJoinedServerListener(),
                new MooLoggingListener()
        );

        // packet adapter
        PacketAdapting.getInstance().register(
                new PacketConfigListener(),
                new PacketPingListener(),
                new PacketRequestListener(),
                new DaemonServerListener(),
                new PacketConsoleInputListener(),
                // database stuff
                new PacketDatabaseInfoListener(),
                new PacketDatabaseModifyListener(),
                new PacketDatabaseCountListener(),
                // player stuff
                new PacketPlayerStateListener(),
                new PacketPlayerInfoListener(),
                new PacketPlayerMessageListener(),
                new PacketPlayerPunishListener(),
                new PacketServerInfoUpdateListener()
        );
        Cloud.getInstance().getLogger().debug("Finished registering listeners.");
    }

    @Override
    protected void onDisable() {
        EventExecutor.getInstance().unregisterAll();
        PacketAdapting.getInstance().unregisterAll();
    }
}
