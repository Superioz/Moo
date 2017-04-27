package de.superioz.moo.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

    /**
     * The priority to sort the event handler
     * That means if you have two listeners and one of them has the priority highest,
     * then highest will be executed first<br>
     * <p>
     * HIGHEST = first<br>
     * HIGH = after HIGHEST<br>
     * MEDIUM = after HIGH<br>
     * LOW = after MEDIUM<br>
     * LOWEST = after LOW<br>
     *
     * @return The event priority
     */
    EventPriority priority() default EventPriority.MEDIUM;

}
