package de.superioz.moo.protocol;

import de.superioz.moo.protocol.packets.*;
import lombok.Getter;
import de.superioz.moo.protocol.packet.AbstractPacket;

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
    PLAYER_INFO(PacketPlayerProfile.class),
    PLAYER_MESSAGE(PacketPlayerMessage.class),
    PLAYER_KICK(PacketPlayerKick.class),
    PLAYER_PUNISH(PacketPlayerBan.class),
    CONFIG(PacketConfig.class),
    REQUEST(PacketRequest.class),
    UPDATE_PERMISSION(PacketUpdatePermission.class),
    SERVER_RAM_USAGE(PacketRamUsage.class),
    SERVER_REQUEST(PacketServerRequest.class),
    SERVER_REQUEST_SHUTDOWN(PacketServerRequestShutdown.class),
    SERVER_ATTEMPT(PacketServerAttempt.class),
    SERVER_REGISTER(PacketServerRegister.class),
    SERVER_UNREGISTER(PacketServerUnregister.class),
    CONSOLE_OUTPUT(PacketConsoleOutput.class),
    CONSOLE_INPUT(PacketConsoleInput.class),
    SERVER_INFO_UPDATE(PacketServerInfoUpdate.class),
    PATTERN_STATE(PacketPatternState.class)
    ;

    public static final int PROTOCOL_VERSION = values().length + 42;

    @Getter
    private Class<? extends AbstractPacket> packetClass;

    Protocol(Class<? extends AbstractPacket> c) {
        this.packetClass = c;
    }


}
