package de.superioz.moo.api.collection;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of an {@link ArrayList} with a fixed size.
 * {@link #addAll(Collection)} is significantly slower than the normal implementation
 *
 * @param <T> The type
 */
public class FixedSizeList<T> extends ArrayList<T> {

    @Getter
    private int maxCapacity;

    public FixedSizeList(int maxCapacity) {
        super(maxCapacity);
        this.maxCapacity = maxCapacity;
    }

    public FixedSizeList(Collection<? extends T> c, int maxCapacity) {
        this(maxCapacity);
        this.addAll(c);
    }

    /**
     * Returns a modifiable list with adding all contents to a normal list
     *
     * @return A modifiable list
     */
    public List<T> toModifiableList() {
        return new ArrayList<>(this);
    }

    @Override
    public boolean add(T t) {
        return size() != maxCapacity && super.add(t);
    }

    @Override
    public void add(int index, T element) {
        if(size() == maxCapacity) return;
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for(T t : c) {
            if(!add(t)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        for(T t : c) {
            if(size() == maxCapacity) return false;
            add(index, t);
        }
        return true;
    }

}
