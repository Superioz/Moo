package de.superioz.moo.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created on 17.09.2016.
 */
@AllArgsConstructor
public enum EventPriority {

    LOWEST(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    HIGHEST(4);

    @Getter
    private int value;

}
