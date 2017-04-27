package de.superioz.moo.api.util;

import lombok.Getter;

@Getter
public enum SpecialCharacter {

    BLOCK_SPACER("┃", "%b"),
    DOUBLE_ARROW("»", "%a"),
    CROSS("✘", "%nok"),
    CHECK("✔", "%ok"),
    BOLD_ARROW("➽", "%ba"),
    HEART("❤", "%h"),
    LYING_HEART("❥", "%lh"),
    CIRCLE("●", "%c");

    private String symbol;
    private String placeholder;

    SpecialCharacter(String symbol, String placeHolder) {
        this.symbol = symbol;
        this.placeholder = placeHolder;
    }

    /**
     * Apply special characters
     *
     * @param before The string with placeholder
     * @return The new string
     */
    public static String apply(String before) {
        for(SpecialCharacter ch : values()) {
            before = before.replace(ch.getPlaceholder(), ch.getSymbol());
        }
        return before;
    }

}
