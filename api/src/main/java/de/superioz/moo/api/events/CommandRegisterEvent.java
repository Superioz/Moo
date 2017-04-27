package de.superioz.moo.api.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.event.Event;

@Getter
@AllArgsConstructor
public class CommandRegisterEvent implements Event {

    private CommandInstance instance;

}
