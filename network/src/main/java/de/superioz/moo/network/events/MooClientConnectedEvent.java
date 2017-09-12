package de.superioz.moo.network.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import de.superioz.moo.api.event.Event;
import de.superioz.moo.network.client.MooClient;

/**
 * This server is called when a client connects to the cloud (a bungee instance, a spigot instance, etc.)
 */
@AllArgsConstructor
@Getter
public class MooClientConnectedEvent implements Event {

    private MooClient client;

}
