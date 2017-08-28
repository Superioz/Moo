package de.superioz.moo.api.logging;

import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;

import java.util.regex.Pattern;

public enum ConsoleColor {

    BLACK('0', "black"),
    DARK_BLUE('1', "dark_blue"),
    DARK_GREEN('2', "dark_green"),
    DARK_AQUA('3', "dark_aqua"),
    DARK_RED('4', "dark_red"),
    DARK_PURPLE('5', "dark_purple"),
    GOLD('6', "gold"),
    GRAY('7', "gray"),
    DARK_GRAY('8', "dark_gray"),
    BLUE('9', "blue"),
    GREEN('a', "green"),
    AQUA('b', "aqua"),
    RED('c', "red"),
    LIGHT_PURPLE('d', "light_purple"),
    YELLOW('e', "yellow"),
    WHITE('f', "white"),

    MAGIC('k', "obfuscated"),
    BOLD('l', "bold"),
    STRIKETHROUGH('m', "strikethrough"),
    UNDERLINE('n', "underline"),
    ITALIC('o', "italic"),
    RESET('r', "reset");

    public static final char COLOR_CHAR = '\u00A7';
    public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");

    public static final String DARK_GRAY_SPECTRUM = "013458";
    public static final String GRAY_SPECTRUM = "6792c";
    public static final String WHITE_SPECTRUM = "abdef";

    @Getter
    private final char code;
    @Getter
    private final String name;
    private final String toString;

    ConsoleColor(char code, String name) {
        this.code = code;
        this.name = name;
        this.toString = new String(new char[]{COLOR_CHAR, code});
    }

    @Override
    public String toString() {
        return toString;
    }

    /**
     * Strips the given message of all color codes
     *
     * @param input String to strip of color
     * @return A copy of the input string, without any coloring
     */
    public static String stripColors(String input) {
        if(input == null) {
            return null;
        }
        for(String s : StringUtil.find("(?i)(§|&)[0-9A-FK-OR]", input)) {
            input = input.replace(s, "");
        }
        return input;
    }

    /**
     * Translates the colors
     *
     * @param altColorChar    The color char
     * @param textToTranslate The text
     * @return The colored text
     */
    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for(int i = 0; i < b.length - 1; i++) {
            if(b[i] == altColorChar && ALL_CODES.indexOf(b[i + 1]) > -1) {
                b[i] = ConsoleColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public static String translateLowSpectrum(char altColorChar, String textToTranslate){
        char[] b = textToTranslate.toCharArray();
        for(int i = 0; i < b.length - 1; i++) {
            if(b[i] == altColorChar && ALL_CODES.indexOf(b[i + 1]) > -1) {
                char c = b[i + 1];

                // the only difference between normal and low spectrum
                if(DARK_GRAY_SPECTRUM.contains((c + "").toLowerCase())){
                    b[i + 1] = '8';
                }
                else if(GRAY_SPECTRUM.contains((c + "").toLowerCase())){
                    b[i + 1] = '7';
                }
                else if(WHITE_SPECTRUM.contains((c + "").toLowerCase())){
                    b[i + 1] = 'f';
                }
                b[i] = ConsoleColor.COLOR_CHAR;
            }
        }
        return new String(b);
    }

}
