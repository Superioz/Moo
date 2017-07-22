package de.superioz.moo.api.events;

import de.superioz.moo.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MooLoggingEvent implements Event {

    private String message;

}
