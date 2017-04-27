package de.superioz.moo.api.event;

public interface Cancellable {

    /**
     * Sets the cancelled-state
     *
     * @param b The new state
     */
    void setCancelled(boolean b);

    /**
     * Checks for the cancelled-state
     *
     * @return The state
     */
    boolean isCancelled();

}
