package de.superioz.moo.client.events;

import de.superioz.moo.api.event.Event;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Called when this client got disconnected from the cloud
 */
@NoArgsConstructor
@Getter
public class CloudDisconnectedEvent implements Event {

}
