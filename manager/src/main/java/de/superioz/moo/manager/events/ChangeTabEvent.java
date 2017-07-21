package de.superioz.moo.manager.events;

import de.superioz.moo.api.event.Event;
import de.superioz.moo.manager.object.Tab;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ChangeTabEvent implements Event {

    private Tab usedButton;

}
