package de.superioz.moo.api.collection;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of a list where the entries can be retrieves like pages
 * <br>
 * Example: You have 20 entries and each page has a size of 10 -> Would result in 2 pages which can be
 * accessed individually
 *
 * @param <T>
 */
@Getter
public class PageableList<T> {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private List<T> entries = new ArrayList<>();
    private int maxPages;
    private int pageSize;

    public PageableList(List<T> entries, int pageSize, Comparator<T>... comparators) {
        if(pageSize < 0) pageSize = DEFAULT_PAGE_SIZE;

        this.entries = entries;
        this.pageSize = pageSize;

        calculatePages();
        if(comparators.length != 0) {
            entries.sort((o1, o2) -> {
                if(o1 == null || o2 == null) return 0;
                int r = 0;
                for(Comparator<T> c : comparators) {
                    r = c.compare(o1, o2);
                }
                return r;
            });
        }
    }

    public PageableList(List<T> entries, Comparator<T>... comparators) {
        this(entries, DEFAULT_PAGE_SIZE, comparators);
    }

    /**
     * Calculates how many pages are there
     */
    public void calculatePages() {
        if(pageSize > 0) {
            maxPages = entries.size() / pageSize;

            if(entries.size() % pageSize != 0) {
                maxPages += 1;
            }
            maxPages -= 1;
        }
    }

    /**
     * Checks if the page is valid
     *
     * @param page The page
     * @return The result
     */
    public boolean checkPage(int page) {
        return page >= 0 && page <= getMaxPages();
    }

    /**
     * Gets the size of the entries
     *
     * @return The size as int
     */
    public int size() {
        return getEntries().size();
    }

    /**
     * Gets the entries from {@link #entries} with given page<br>
     * Calculation as followed:
     * minIndex = {@code page} * {@link #pageSize} and maxIndex = minIndex + {@link #pageSize}<br>
     * Empty entries will be replaced with given {@code nullObject}
     *
     * @param page       The page (0-{@link #maxPages})
     * @param nullObject The placeholder object
     * @return The list of entries
     */
    public List<T> getPage(int page, T nullObject) {
        if(page < 0 || page > maxPages) return null;
        int minIndex = page * pageSize;
        int maxIndex = minIndex + pageSize;

        List<T> entries = getEntries();
        List<T> currentPage = new ArrayList<>();

        for(int i = minIndex; i < maxIndex; i++) {
            if(i >= entries.size()) {
                currentPage.add(nullObject);
            }
            else {
                currentPage.add(entries.get(i));
            }
        }

        return currentPage;
    }

    public List<T> getPage(int page) {
        return getPage(page, null);
    }

    /**
     * Checks if the list does not have any entries
     *
     * @return The result
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

}
