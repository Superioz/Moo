package de.superioz.moo.minecraft.chat.formats;

import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.common.Replacor;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.minecraft.util.ChatUtil;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class PageableListFormat<T> extends DisplayFormat {

    private PageableList<T> pageableList;
    private int page;
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
            addComponent(emptyListMessage);
            return;
        }
        if(page < 0 || page > pageableList.getMaxPages()) return;

        String seperationFormat = LanguageManager.get("list-format-seperation", header, page + 1, pageableList.getMaxPages() + 1);
        addComponent(seperationFormat);

        // content
        pageableList.getPage(page, null).forEach(t -> {
            if(t == null) {
                addComponent(emptyEntry);
                return;
            }

            Replacor<T> replacor = new Replacor<>(t);
            entryConsumer.accept(replacor);

            String entry = StringUtil.format(entryFormat, replacor.getReplacements());
            addComponent(ChatUtil.getEventMessage(entry, condition == null ? true : condition.apply(t)).toTextComponent());
        });

        // footer
        if(page < pageableList.getMaxPages() && footer != null && !footer.isEmpty()) {
            addComponent("");
            addComponent(footer);
        }

        addComponent(seperationFormat);
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

    public PageableListFormat<T> emptyList(String emptyList) {
        this.emptyListMessage = getMessage(emptyList);
        return this;
    }

    public PageableListFormat<T> header(String header) {
        this.header = getMessage(header);
        return this;
    }

    public PageableListFormat<T> emptyEntry(String emptyEntry) {
        this.emptyEntry = getMessage(emptyEntry);
        return this;
    }

    /*
    ===================
    OTHERS
    ===================
     */

    public PageableListFormat<T> entry(String entryFormat) {
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
