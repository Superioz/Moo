package de.superioz.moo.network.events;

import de.superioz.moo.network.queries.Queries;
import de.superioz.moo.network.queries.Response;
import de.superioz.moo.network.packet.AbstractPacket;
import lombok.Getter;
import lombok.Setter;
import de.superioz.moo.api.event.Cancellable;
import de.superioz.moo.api.event.Event;
import de.superioz.moo.api.event.Supplyable;
import de.superioz.moo.api.util.LazySupplier;

/**
 * The event to call to query something from the database with {@link Queries}
 */
public class QueryEvent implements Event, Supplyable<Response>, Cancellable {

    private LazySupplier<Response> supplier = new LazySupplier<>();

    private boolean cancelled = false;
    @Setter @Getter
    private Throwable cancelReason;

    /**
     * The packet to be either sent or simulated
     */
    @Getter
    private AbstractPacket toQueryPacket;

    public QueryEvent(AbstractPacket toQueryPacket) {
        this.toQueryPacket = toQueryPacket;
    }

    @Override
    public LazySupplier<Response> getSupplier() {
        return supplier;
    }

    @Override
    public void accept(Response response) {
        if(!supplier.isEmpty()) return;
        supplier.accept(response);
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
