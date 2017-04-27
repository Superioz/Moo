package de.superioz.moo.api.event;

import de.superioz.moo.api.util.LazySupplier;

public interface Supplyable<T> {

    LazySupplier<T> getSupplier();

    void accept(T t);

}
