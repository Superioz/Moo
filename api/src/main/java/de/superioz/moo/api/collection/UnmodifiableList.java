package de.superioz.moo.api.collection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Just a {@link ArrayList} but unmodifiable. Simply as that.<br>
 * To do that it overrides the methods from the list which would edit the objects inside
 * the list
 *
 * @param <T> The type of the list
 */
public class UnmodifiableList<T> extends ArrayList<T> {

    public UnmodifiableList(Collection<? extends T> c) {
        super(c);
    }

    public boolean add(int index) {
        return false;
    }

    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    public T remove(int index) {
        return null;
    }

}
