package de.superioz.moo.api.reaction;

import de.superioz.moo.api.util.Procedure;
import lombok.Getter;
import lombok.Setter;

/**
 * Similar class to {@link Procedure} but with a {@code object} as condition to be executed
 * @param <T> The type of the object
 */
public abstract class Reactor<T> {

    @Getter
    private T object;

    @Getter @Setter
    private boolean cancelled = false;

    //
    public Reactor(T object) {
        this.object = object;
    }

    /**
     * Simply invokes this reactor by executing this method.<br>
     * Similar to {@link Procedure#invoke()}
     *
     * @see #object Difference between procedure and this class
     */
    public abstract void invoke();

}
