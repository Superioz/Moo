package de.superioz.moo.manager.events;

import de.superioz.moo.api.event.Event;
import de.superioz.moo.manager.entity.TabbedButton;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ChangeTabEvent implements Event {

    private TabbedButton usedButton;

}
