package de.superioz.moo.client.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import de.superioz.moo.api.event.Event;
import de.superioz.moo.protocol.common.ResponseStatus;

/**
 * Called when this client is connected to the cloud
 */
@AllArgsConstructor
@Getter
public class ClientConnectedEvent implements Event {

    private ResponseStatus status;

}
