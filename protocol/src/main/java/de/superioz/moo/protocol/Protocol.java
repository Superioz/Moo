package de.superioz.moo.protocol;

import de.superioz.moo.protocol.packets.*;
import lombok.Getter;
import de.superioz.moo.protocol.packet.AbstractPacket;
import net.draxento.protocol.packets.*;

public enum Protocol {

    MULTI(MultiPacket.class),
    KEEPALIVE(PacketKeepalive.class),
    PING(PacketPing.class),
    HANDSHAKE(PacketHandshake.class),
    RESPOND(PacketRespond.class),
    DATABASE_INFO(PacketDatabaseInfo.class),
    DATABASE_INFO_RAW(PacketDatabaseInfoNative.class),
    DATABASE_MODIFY(PacketDatabaseModify.class),
    DATABASE_MODIFY_RAW(PacketDatabaseModifyNative.class),
    DATABASE_COUNT(PacketDatabaseCount.class),
    PLAYER_STATE(PacketPlayerState.class),
    PLAYER_INFO(PacketPlayerInfo.class),
    PLAYER_MESSAGE(PacketPlayerMessage.class),
    PLAYER_KICK(PacketPlayerKick.class),
    PLAYER_PUNISH(PacketPlayerPunish.class),
    CONFIG(PacketConfig.class),
    REQUEST(PacketRequest.class),
    SERVER_RAM_USAGE(PacketRamUsage.class),
    SERVER_REQUEST(PacketServerRequest.class),
    SERVER_REQUEST_SHUTDOWN(PacketServerRequestShutdown.class),
    SERVER_ATTEMPT(PacketServerAttempt.class),
    SERVER_DONE(PacketServerDone.class),
    SERVER_REGISTER(PacketServerRegister.class),
    SERVER_UNREGISTER(PacketServerUnregister.class);

    public static final int PROTOCOL_VERSION = values().length + 42;

    @Getter
    private Class<? extends AbstractPacket> packetClass;

    Protocol(Class<? extends AbstractPacket> c) {
        this.packetClass = c;
    }


}
