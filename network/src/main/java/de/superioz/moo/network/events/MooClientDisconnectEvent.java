package de.superioz.moo.network.events;

import de.superioz.moo.api.event.Event;
import de.superioz.moo.network.server.MooClient;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This server is called when a client disconnects from the cloud (a bungee instance, a spigot instance, etc.)
 */
@AllArgsConstructor
@Getter
public class MooClientDisconnectEvent implements Event {

    private MooClient client;

}
