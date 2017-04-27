package de.superioz.moo.api.command.param;

import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;
import de.superioz.moo.api.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This enum holds regex to check the value from {@link ParamFilter}
 *
 * @see ParamFilter#value
 */
public enum ParamFilterAttribute {

    RANGE("([0-9]+(.[0-9]+)?)[-]([0-9]+(.[0-9]+)?)"),
    GREATER_THAN(">([0-9]+(.[0-9]+)?)"),
    GREATER_THAN_EQUALS(">=([0-9]+(.[0-9]+)?)"),
    LOWER_THAN("<([0-9]+(.[0-9]+)?)"),
    LOWER_THAN_EQUALS("<=([0-9]+(.[0-9]+)?)"),
    POSITIVE("[+]"),
    NEGATIVE("[-]"),
    EQUALITY("([0-9]+(.[0-9]+)?)(,([0-9]+(.[0-9]+)?))*"),
    CUSTOM("[a-zA-Z_]+"),
    TYPE(""),
    UNKNOWN("");

    @Getter
    private String regex;

    ParamFilterAttribute(String regex) {
        this.regex = regex;
    }

    /**
     * Gets a param def attribute from given string
     *
     * @param s The string
     * @return The attribute
     */
    public static ParamFilterAttribute from(String s) {
        for(ParamFilterAttribute attribute : values()) {
            if(Pattern.compile(attribute.getRegex()).matcher(s).matches()) return attribute;
        }
        return UNKNOWN;
    }

    /**
     * Get values from given attribute and regex-checked string
     *
     * @param attribute The attribute
     * @param str       The string
     * @return The list of values
     */
    public static List<String> getValuesFrom(ParamFilterAttribute attribute, String str) {
        List<String> l = new ArrayList<>();

        switch(attribute) {
            case RANGE: {
                l.addAll(StringUtil.splitWithoutEmpty(str, "-"));
                break;
            }
            case GREATER_THAN: {
                l.addAll(StringUtil.splitWithoutEmpty(str, ">"));
                break;
            }
            case GREATER_THAN_EQUALS: {
                l.addAll(StringUtil.splitWithoutEmpty(str, ">="));
                break;
            }
            case LOWER_THAN: {
                l.addAll(StringUtil.splitWithoutEmpty(str, "<"));
                break;
            }
            case LOWER_THAN_EQUALS: {
                l.addAll(StringUtil.splitWithoutEmpty(str, "<="));
                break;
            }
            case POSITIVE:
            case NEGATIVE:
            case TYPE:
            case CUSTOM:
                break;
            case EQUALITY: {
                l.addAll(StringUtil.splitWithoutEmpty(str, ","));
                break;
            }
        }
        return l;
    }

    /**
     * Checks given argument with type and the string which fits to this regex
     *
     * @param type The type
     * @param s    The string
     * @param arg  The argument
     * @param <T>  The type of param
     * @return The result
     */
    public <T> boolean check(ParamType<T> type, String s, String arg) {
        switch(this) {
            case RANGE: {
                String[] s0 = s.split("-");
                Object o1 = ReflectionUtil.safeCast(s0[0]);
                Object o2 = ReflectionUtil.safeCast(s0[1]);

                return type.checkRange(arg, o1, o2);
            }
            case GREATER_THAN: {
                Object o1 = ReflectionUtil.safeCast(s.split(">")[1]);
                return type.checkGreaterThan(arg, o1);
            }
            case GREATER_THAN_EQUALS: {
                return type.checkGreaterThanEquals(arg, ReflectionUtil.safeCast(s.split(">=")[1]));
            }
            case LOWER_THAN: {
                return type.checkLowerThan(arg, ReflectionUtil.safeCast(s.split("<")[1]));
            }
            case LOWER_THAN_EQUALS: {
                return type.checkGreaterThanEquals(arg, ReflectionUtil.safeCast(s.split("<=")[1]));
            }
            case POSITIVE: {
                return type.checkPositivity(arg);
            }
            case NEGATIVE: {
                return type.checkNegativity(arg);
            }
            case EQUALITY: {
                return type.checkEquality(arg, Arrays.asList(s.split(",")));
            }
            case CUSTOM: {
                return type.checkCustom(arg, s);
            }
            default:
                return true;
        }
    }

}
