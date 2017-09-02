package de.superioz.moo.network.packet;

import de.superioz.moo.api.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods inside a {@link PacketAdapter} class<br>
 * These methods needs following erasure: public void $name($packet_class) and with this annotation
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PacketHandler {

    /**
     * Priority of the event
     * @return The event priority object
     * @see EventPriority
     */
    EventPriority priority() default EventPriority.MEDIUM;

}
