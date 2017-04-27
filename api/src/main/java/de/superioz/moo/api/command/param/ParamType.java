package de.superioz.moo.api.command.param;

import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.NumberUtil;
import de.superioz.moo.api.util.Validation;

import java.util.List;

/**
 * This parameter class is used to automatically validate parameters inside a command method.<br>
 * To fetch a custom param type you have to use {@link GenericParameterSet#get(int, Class)}
 *
 * @param <E> The type
 */
public abstract class ParamType<E> {

    public abstract String label();

    public abstract E resolve(String s);

    public abstract Class<E> typeClass();

    /**
     * Checks if the argument is between given range
     *
     * @param arg The argument
     * @param t1  The first value
     * @param t2  The second value
     * @param <T> The type
     * @return The result
     */
    public <T> boolean checkRange(String arg, T t1, T t2) {
        return Validation.NUMBER.matches(arg)
                && NumberUtil.checkRange(ReflectionUtil.safeCast(arg), t1, t2);
    }

    /**
     * Checks if the argument is greater than the value
     *
     * @param arg The argument
     * @param t1  The value
     * @param <T> The type
     * @return The result
     */
    public <T> boolean checkGreaterThan(String arg, T t1) {
        return Validation.NUMBER.matches(arg)
                && NumberUtil.checkPosition(ReflectionUtil.safeCast(arg), t1, true, false);
    }

    /**
     * Checks if the argument is greater than or equals the value
     *
     * @param arg The argument
     * @param t1  The value
     * @param <T> The type
     * @return The result
     */
    public <T> boolean checkGreaterThanEquals(String arg, T t1) {
        return Validation.NUMBER.matches(arg)
                && NumberUtil.checkPosition(ReflectionUtil.safeCast(arg), t1, true, true);
    }

    /**
     * Checks if the argument is lower than the value
     *
     * @param arg The argument
     * @param t1  The value
     * @param <T> The type
     * @return The result
     */
    public <T> boolean checkLowerThan(String arg, T t1) {
        return Validation.NUMBER.matches(arg)
                && NumberUtil.checkPosition(ReflectionUtil.safeCast(arg), t1, false, false);
    }

    /**
     * Checks if the argument is lower than or equals the value
     *
     * @param arg The argument
     * @param t1  The value
     * @param <T> The type
     * @return The result
     */
    public <T> boolean checkLowerThanEquals(String arg, T t1) {
        return Validation.NUMBER.matches(arg)
                && NumberUtil.checkPosition(ReflectionUtil.safeCast(arg), t1, false, true);
    }

    /**
     * Checks if the argument is positive
     *
     * @param arg The argument
     * @return The result
     */
    public boolean checkPositivity(String arg) {
        return !checkNegativity(arg);
    }

    /**
     * Checks if the argument is negative
     *
     * @param arg The argument
     * @return The result
     */
    public boolean checkNegativity(String arg) {
        return arg.startsWith("-");
    }

    /**
     * Checks if the given list contains given arf
     *
     * @param arg The argument
     * @param ts  The list
     * @param <T> The type
     * @return The result
     */
    public <T> boolean checkEquality(String arg, List<T> ts) {
        T o = (T) ReflectionUtil.safeCast(arg);
        return ts.contains(o);
    }

    /**
     * Checks if a custom attribute is given
     *
     * @param arg The argument
     * @param s   The custom attribute
     * @return The result
     */
    public abstract boolean checkCustom(String arg, String s);

    public abstract String handleCustomException(String s);

}
