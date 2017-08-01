package de.superioz.moo.proxy.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerSpecificInfo {

    private String motd;
    private int onlinePlayers;
    private int maxPlayers;

}
