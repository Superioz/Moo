package de.superioz.moo.cloud.events;

import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * We want to restock the server!
 */
@AllArgsConstructor
@Getter
public class MooServerRestockEvent implements Event {

    private ServerPattern pattern;

}
