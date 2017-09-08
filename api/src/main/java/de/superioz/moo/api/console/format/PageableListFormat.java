package de.superioz.moo.api.console.format;

import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.common.Replacor;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class PageableListFormat<T> extends DisplayFormat {

    private PageableList<T> pageableList;
    private int page;
    private String pageDoesntExist;
    private String emptyListMessage;
    private String header;
    private String emptyEntry;
    private String entryFormat;

    private String footer;

    private Consumer<Replacor<T>> entryConsumer;
    private Function<T, Boolean> condition;

    public PageableListFormat(PageableList pageableList) {
        this.pageableList = pageableList;
    }

    @Override
    public void setupComponents() {
        if(pageableList.size() == 0) {
            addMessage(emptyListMessage);
            return;
        }
        if(!pageableList.checkPage(page)){
            addMessage(pageDoesntExist);
            return;
        }

        String seperationFormat = LanguageManager.contains("list-format-seperation")
                        ? LanguageManager.get("list-format-seperation", header, page + 1, pageableList.getMaxPages() + 1)
                        : StringUtil.repeat("=", 20) + header + StringUtil.repeat("=", 20);
        addMessage(seperationFormat);

        // content
        pageableList.getPage(page, null).forEach(t -> {
            if(t == null) {
                addMessage(emptyEntry);
                return;
            }

            Replacor<T> replacor = new Replacor<>(t);
            entryConsumer.accept(replacor);

            String entry = StringUtil.format(entryFormat, replacor.getReplacements());
            addMessage(entry, condition == null ? true : condition.apply(t));
        });

        // footer
        if(page < pageableList.getMaxPages() && footer != null && !footer.isEmpty()) {
            addMessage("");
            addMessage(footer);
        }

        addMessage(seperationFormat);
    }

    /*
    ===================
    SETTER
    ===================
     */

    public PageableListFormat<T> page(int page) {
        this.page = page;
        return this;
    }

    public PageableListFormat<T> doesntExist(String pageDoesntExist){
        this.pageDoesntExist = pageDoesntExist;
        return this;
    }

    public PageableListFormat<T> emptyList(String emptyList, Object... replacements) {
        this.emptyListMessage = getMessage(emptyList, replacements);
        return this;
    }

    public PageableListFormat<T> header(String header, Object... replacements) {
        this.header = getMessage(header, replacements);
        return this;
    }

    public PageableListFormat<T> emptyEntry(String emptyEntry, Object... replacements) {
        this.emptyEntry = getMessage(emptyEntry, replacements);
        return this;
    }

    /*
    ===================
    OTHERS
    ===================
     */

    public PageableListFormat<T> entryFormat(String entryFormat) {
        this.entryFormat = getMessage(entryFormat);
        return this;
    }

    /**
     * Sets the entry format with given condition
     *
     * @param entryConsumer The entry consumer
     * @param condition     The condition
     * @return This
     */
    public PageableListFormat<T> entry(Consumer<Replacor<T>> entryConsumer, Function<T, Boolean> condition) {
        this.entryConsumer = entryConsumer;
        this.condition = condition;
        return this;
    }

    public PageableListFormat<T> entry(Consumer<Replacor<T>> entryConsumer) {
        return entry(entryConsumer, null);
    }

    /**
     * Sets the footer and the replacor to set
     *
     * @param footer   The footer
     * @return This
     */
    public PageableListFormat<T> footer(String footer, Object... replacements) {
        this.footer = getMessage(footer, replacements);
        return this;
    }

}
