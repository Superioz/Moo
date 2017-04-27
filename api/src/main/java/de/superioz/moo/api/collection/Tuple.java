package de.superioz.moo.api.collection;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Just a {@link ArrayList} containing sub lists. This is to store n-values into a list, where they all belong to each other.
 *
 * @see ArrayList
 * @see FixedSizeList
 */
@Getter
public class Tuple<T> {

    public static final int DEFAULT_ENTRY_SIZE = 2;

    private int entrySize = DEFAULT_ENTRY_SIZE;
    private List<FixedSizeList<T>> entries = new ArrayList<>();

    public Tuple(int entrySize, List<T>... initialEntries) {
        this.entrySize = entrySize;
        for(List<T> objectList : initialEntries) {
            if(objectList.size() > entrySize) {
                objectList = objectList.subList(0, entrySize - 1);
            }
            FixedSizeList fixedSizeList = new FixedSizeList(objectList, entrySize);
            entries.add(fixedSizeList);
        }
    }

    public Tuple(List<T>... initialEntries) {
        this(DEFAULT_ENTRY_SIZE, initialEntries);
    }

    public Tuple(int entrySize, T... initialEntries) {
        this(entrySize, Arrays.asList(initialEntries));
    }

    /**
     * Turns all the list entries into a normal entry list
     *
     * @param listEConverter The converter of a list into a single object
     * @param <E>            The type of the new list (e.g. {@link String})
     * @return The list of new objects
     */
    public <E> List<E> toList(Function<List<T>, E> listEConverter) {
        List<E> elementList = new ArrayList<>();
        for(FixedSizeList l : getEntries()) {
            elementList.add(listEConverter.<List<T>, E>apply((List<T>) l.toModifiableList()));
        }
        return elementList;
    }

    /**
     * Gets the fixed size list at given index
     *
     * @param index The index (not working if index is invalid)
     * @return The list
     */
    public FixedSizeList<T> get(int index) {
        if(index >= entries.size() || index < 0) return null;
        return entries.get(index);
    }

    /**
     * Gets the list which contains given value
     *
     * @param keyValue The key of the value
     * @return The list
     * @see #get(int)
     */
    public FixedSizeList<T> get(Object keyValue) {
        if(keyValue == null) return null;
        for(FixedSizeList l : entries) {
            if(l.contains(keyValue)) return l;
        }
        return null;
    }

    /**
     * Sets given list to given index
     *
     * @param index The index
     * @param list  The list
     * @return The list that has been set
     */
    public FixedSizeList<T> set(int index, FixedSizeList<T> list) {
        if(list.getMaxCapacity() != entrySize) return null;
        return entries.set(index, list);
    }

    public FixedSizeList<T> set(int index, List<T> list) {
        if(list.size() > entrySize) list = list.subList(0, entrySize - 1);
        return set(index, new FixedSizeList<>(list, entrySize));
    }

    /**
     * Adds given entries to a list and add it to the entry list of this tuple
     *
     * @param subEntries The sub entries
     * @return This
     */
    public Tuple add(T... subEntries) {
        entries.add(new FixedSizeList(Arrays.asList(subEntries), entrySize));
        return this;
    }

    public Tuple add(FixedSizeList<T> list) {
        if(list.getMaxCapacity() == entrySize) {
            entries.add(list);
        }
        return this;
    }

    public Tuple add(List<T> list) {
        if(list.size() > entrySize) list = list.subList(0, entrySize - 1);
        return add(new FixedSizeList<>(list, entrySize));
    }

    /**
     * Adds multiple entries to the entrylist of this tuple
     *
     * @param entries The entries as list
     * @return This
     */
    public Tuple addAll(FixedSizeList<T>... entries) {
        for(FixedSizeList l : entries) {
            add(l);
        }
        return this;
    }

    public Tuple addAll(List<T>... entries) {
        for(List l : entries) {
            add(l);
        }
        return this;
    }

}
