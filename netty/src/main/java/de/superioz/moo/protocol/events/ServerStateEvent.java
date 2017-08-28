package de.superioz.moo.protocol.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import de.superioz.moo.api.event.Event;
import de.superioz.moo.protocol.server.NetworkServer;

/**
 * Event called when a {@link NetworkServer} changed its state
 */
@AllArgsConstructor
@Getter
public class ServerStateEvent implements Event {

    /**
     * The server
     */
    private NetworkServer server;

    /**
     * The server state
     */
    private NetworkServer.State state;

}
