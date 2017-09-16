package de.superioz.moo.api.command.param;

import de.superioz.moo.api.exceptions.CommandException;
import lombok.Getter;
import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.util.Operator;
import de.superioz.moo.api.utils.NumberUtil;
import de.superioz.moo.api.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to filter a number. That means to be able
 * to check if the value is in range of or greater than other values or whatever<br>
 * Attributes' syntax is checked with {@link ParamFilterAttribute#getRegex()}
 *
 * @param <T> The type of number (e.g. {@link Integer} or {@link Double})
 */
@Getter
public class ParamFilter<T extends Number> {

    /**
     * The attributes that have to be true for given number
     *
     * @see #value
     */
    private Map<String, ParamFilterAttribute> attributes;

    /**
     * The number
     */
    private T value;

    /**
     * The class of the number
     */
    private Class<T> typeClass;

    public ParamFilter(T value) {
        this.value = value;
        this.typeClass = (Class<T>) value.getClass();
        this.attributes = new HashMap<>();
    }

    /**
     * Checks if the filter is true
     *
     * @return The result
     */
    public boolean check() {
        ParamType<T> type = CommandRegistry.getInstance().getParamTypeRegistry().get(getTypeClass());
        if(type == null) return false;
        boolean r = false;
        for(String s : attributes.keySet()) {
            String arg = value + "";
            ParamFilterAttribute attribute = attributes.get(s);

            r = attribute.check(type, s, arg);
            if(!r) {
                if(attribute == ParamFilterAttribute.CUSTOM) {
                    throw new CommandException(CommandException.Type.CUSTOM, type.handleCustomException(s));
                }
                String n = attribute == ParamFilterAttribute.NEGATIVE ? "<0" : attribute == ParamFilterAttribute.POSITIVE ? ">0" : s;
                throw new CommandException(CommandException.Type.VALIDATE, arg, n);
            }
        }
        return r;
    }

    /**
     * Adds an attribute to the filter
     *
     * @param s The syntax
     * @return This
     */
    public ParamFilter addAttribute(String s) {
        attributes.put(s, ParamFilterAttribute.from(s));
        return this;
    }

    public ParamFilter addAttribute(Operator operator, T t) {
        return addAttribute(operator.getSymbol() + t);
    }

    /**
     * The param must be greater than given number
     *
     * @param number The number
     * @return This
     */
    public ParamFilter gt(T number) {
        return addAttribute(Operator.GREATER_THAN, number);
    }

    /**
     * The param must be greater than or equals given number
     *
     * @param number The number
     * @return This
     */
    public ParamFilter gte(T number) {
        return addAttribute(Operator.GREATER_THAN_OR_EQUALS, number);
    }

    /**
     * The param must be lower than given number
     *
     * @param number The number
     * @return This
     */
    public ParamFilter lt(T number) {
        return addAttribute(Operator.LESS_THAN, number);
    }

    /**
     * The param must be lower than or equals given number
     *
     * @param number The number
     * @return This
     */
    public ParamFilter lte(T number) {
        return addAttribute(Operator.LESS_THAN_OR_EQUALS, number);
    }

    /**
     * The param must be between given numbers exclusive
     *
     * @param n1 Minimum val
     * @param n2 Maximum val
     * @return This
     */
    public ParamFilter rangeExcl(T n1, T n2) {
        n1 = (T) NumberUtil.append(n1, 1);
        n2 = (T) NumberUtil.subtract(n2, 1);

        return addAttribute(n1 + "-" + n2);
    }

    /**
     * The param must be between given numbers inclusive
     *
     * @param n1 Minimum val
     * @param n2 Maximum val
     * @return This
     */
    public ParamFilter rangeIncl(T n1, T n2) {
        return addAttribute(n1 + "-" + n2);
    }

    /**
     * The param must be positive
     *
     * @return This
     */
    public ParamFilter pos() {
        return addAttribute("+");
    }

    /**
     * The param must be negative
     *
     * @return This
     */
    public ParamFilter neg() {
        return addAttribute("-");
    }

    /**
     * The param must be one of given values
     *
     * @param ts The values
     * @return This
     */
    public ParamFilter eq(T... ts) {
        return addAttribute(StringUtil.join(",", (Object[]) ts));
    }

    /**
     * The param must be validated by custom validator
     *
     * @param type The type of validation
     * @return This
     */
    public ParamFilter custom(String type) {
        return addAttribute(type);
    }

}
