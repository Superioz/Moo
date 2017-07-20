package de.superioz.moo.api.events;

import de.superioz.moo.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.logging.LogRecord;

@Getter
@AllArgsConstructor
public class MooLoggingEvent implements Event {

    private LogRecord record;

}
