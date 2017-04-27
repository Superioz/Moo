package de.superioz.moo.api.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionUtil {

    /**
     * Sorts given map by given comparator
     *
     * @param map        The map
     * @param comparator The comparator to sort the map
     * @param <K>        The key type
     * @param <V>        The value type
     * @return The sorted map
     */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map, Comparator<Map.Entry<K, V>> comparator) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort(comparator);

        Map<K, V> result = new LinkedHashMap<>();
        for(Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Filters the list by iterating over the given list and checking the element per predicate
     *
     * @param eList     The elements list to check
     * @param predicate The predicate to validate the content
     * @param <E>       The element type
     * @return The filtered list
     */
    public static <E> List<E> filterList(List<E> eList, Predicate<E> predicate, Comparator<E> comparator) {
        List<E> list = new ArrayList<>();
        for(E e : eList) {
            if(predicate.test(e)) {
                list.add(e);
            }
        }
        if(comparator != null) {
            list.sort(comparator);
        }
        return list;
    }

    public static <E> List<E> filterList(List<E> eList, Predicate<E> predicate) {
        return filterList(eList, predicate, null);
    }

    /**
     * Gets an entry of given list with given index, but safely.<br>
     * That means if the index is greater or lower than the allowed
     * boundaries this method will either use the first index or the last index.<br>
     *
     * @param eList      The element's list
     * @param index      The index to get the entry from
     * @param defaultVal If the value would be null
     * @param <E>        The element's type
     * @return The element (or null if the list is empty)
     */
    public static <E> E getEntrySafely(List<E> eList, int index, E defaultVal) {
        if(index < 0) index *= -1;

        if(eList.isEmpty()) return defaultVal;
        if(index >= eList.size()) index = eList.size() - 1;
        else if(index < 0) index = 0;

        if(index >= eList.size()) return defaultVal;
        return eList.get(index);
    }

}
