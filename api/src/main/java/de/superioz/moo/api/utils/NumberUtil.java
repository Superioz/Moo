package de.superioz.moo.api.utils;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberUtil {

    /**
     * Gets the number unsigned, but as string because it could lead to an overflow error
     *
     * @param t   The number
     * @param <T> The type of number
     * @return The unsigned number as string
     */
    public static <T extends Number> String getUnsigned(T t) {
        String s = t + "";
        if(s.startsWith("-") || s.startsWith("+")) {
            s = s.substring(1, s.length());
        }
        return s;
    }

    /**
     * Check the bounds of an operation
     *
     * @param val1 The first value
     * @param val2 The second value
     * @param add  Addition or subtraction?
     * @param <T>  The numbertype
     * @return The result
     */
    public static <T> boolean checkBounds(T val1, T val2, boolean add) {
        if(val1 == null || val2 == null || !Number.class.isAssignableFrom(val1.getClass())) return false;
        T boundVal = getBoundary((Class<T>) val1.getClass(), !add);
        if(boundVal == null) return false;

        if(add) {
            if(!(NumberUtil.checkPosition(val1, NumberUtil.subtract(boundVal, val2), false, true))) {
                return false;
            }
        }
        else {
            if(!(NumberUtil.checkPosition(val1, NumberUtil.append(boundVal, val2), true, true))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks the position from the second value to the first value
     *
     * @param o1  The first value
     * @param o2  The second value
     * @param gt  Greater than or lower than check?
     * @param eq  gte or gt check?
     * @param <T> The type
     * @return The result
     */
    public static <T> boolean checkPosition(T o1, T o2, boolean gt, boolean eq) {
        if(o1 instanceof Integer && o2 instanceof Integer) {
            return !eq ? (gt ? (Integer) o1 > (Integer) o2 : (Integer) o1 < (Integer) o2)
                    : (gt ? (Integer) o1 >= (Integer) o2 : (Integer) o1 <= (Integer) o2);
        }
        else if(o1 instanceof Double && o2 instanceof Double) {
            return !eq ? (gt ? (Double) o1 > (Double) o2 : (Double) o1 < (Double) o2)
                    : (gt ? (Double) o1 >= (Double) o2 : (Double) o1 <= (Double) o2);
        }
        else if(o1 instanceof Float && o2 instanceof Float) {
            return !eq ? (gt ? (Float) o1 > (Float) o2 : (Float) o1 < (Float) o2)
                    : (gt ? (Float) o1 >= (Float) o2 : (Float) o1 <= (Float) o2);
        }
        else if(o1 instanceof Long && o2 instanceof Long) {
            return !eq ? (gt ? (Long) o1 > (Long) o2 : (Long) o1 < (Long) o2)
                    : (gt ? (Long) o1 >= (Long) o2 : (Long) o1 <= (Long) o2);
        }
        else if(o1 instanceof String && o2 instanceof String) {
            return !eq ? (gt ? ((String) o1).length() > ((String) o2).length() : ((String) o1).length() < ((String) o2).length())
                    : (gt ? ((String) o1).length() >= ((String) o2).length() : ((String) o1).length() <= ((String) o2).length());
        }
        else {
            return !eq ? (gt ? o1.hashCode() > o2.hashCode() : o1.hashCode() < o2.hashCode())
                    : (gt ? o1.hashCode() >= o2.hashCode() : o1.hashCode() <= o2.hashCode());
        }
    }

    /**
     * Checks if the first value is between the other two values
     *
     * @param o0 The first
     * @param o1 The second
     * @param o2 The third
     * @return The result
     */
    public static <T> boolean checkRange(T o0, T o1, T o2) {
        if(o0 instanceof Integer && o1 instanceof Integer && o2 instanceof Integer) {
            return (Integer) o1 <= (Integer) o0 && (Integer) o0 <= (Integer) o2;
        }
        else if(o0 instanceof Double && o1 instanceof Double && o2 instanceof Double) {
            return (Double) o1 <= (Double) o0 && (Double) o0 <= (Double) o2;
        }
        else if(o0 instanceof Float && o1 instanceof Float && o2 instanceof Float) {
            return (Float) o1 <= (Float) o0 && (Float) o0 <= (Float) o2;
        }
        else if(o0 instanceof Long && o1 instanceof Long && o2 instanceof Long) {
            return (Long) o1 <= (Long) o0 && (Long) o0 <= (Long) o2;
        }
        else if(o0 instanceof String && o1 instanceof String && o2 instanceof String) {
            return ((String) o1).length() <= ((String) o0).length() && ((String) o0).length() <= ((String) o2).length();
        }
        else {
            return o1.hashCode() <= o0.hashCode() && o0.hashCode() <= o2.hashCode();
        }
    }

    /**
     * Appends one object to another
     *
     * @param o1 The first object
     * @param o2 The second object
     * @return The result
     */

    public static Object append(Object o1, Object o2) {
        if(o2 instanceof List) {
            for(Object o : (List) o2) {
                o1 = append(o1, o);
            }
            return o1;
        }

        if(o1 instanceof Integer && o2 instanceof Integer) {
            return (Integer) o1 + (Integer) o2;
        }
        else if(o1 instanceof Double && o2 instanceof Double) {
            return (Double) o1 + (Double) o2;
        }
        else if(o1 instanceof Float && o2 instanceof Float) {
            return (Float) o1 + (Float) o2;
        }
        else if(o1 instanceof Long && o2 instanceof Long) {
            return (Long) o1 + (Long) o2;
        }
        else if(o1 instanceof String && o2 instanceof String) {
            return ((String) o1).replace((String) o2, "");
        }
        else if(o1 instanceof List) {
            List l = (List) o1;
            l.add(o2);
            return l;
        }
        else {
            return o1.hashCode() + o2.hashCode();
        }
    }

    /**
     * Subtracts one object from another
     *
     * @param o1 The first object
     * @param o2 The second object
     * @return The result
     */
    public static Object subtract(Object o1, Object o2) {
        if(o2 instanceof List) {
            for(Object o : (List) o2) {
                o1 = subtract(o1, o);
            }
            return o1;
        }

        if(o1 instanceof Integer && o2 instanceof Integer) {
            return (Integer) o1 - (Integer) o2;
        }
        else if(o1 instanceof Double && o2 instanceof Double) {
            return (Double) o1 - (Double) o2;
        }
        else if(o1 instanceof Float && o2 instanceof Float) {
            return (Float) o1 - (Float) o2;
        }
        else if(o1 instanceof Long && o2 instanceof Long) {
            return (Long) o1 - (Long) o2;
        }
        else if(o1 instanceof String && o2 instanceof String) {
            return ((String) o1).concat((String) o2);
        }
        else if(o1 instanceof List) {
            List l = (List) o1;
            l.remove(o2);
            return l;
        }
        else {
            return o1.hashCode() - o2.hashCode();
        }
    }

    /**
     * Subtracts one object from another
     *
     * @param o1 The first object
     * @param o2 The second object
     * @return The result
     */
    public static Object multiply(Object o1, Object o2) {
        if(o2 instanceof List) {
            for(Object o : (List) o2) {
                o1 = multiply(o1, o);
            }
            return o1;
        }

        if(o1 instanceof Integer && o2 instanceof Integer) {
            return (Integer) o1 * (Integer) o2;
        }
        else if(o1 instanceof Double && o2 instanceof Double) {
            return (Double) o1 * (Double) o2;
        }
        else if(o1 instanceof Float && o2 instanceof Float) {
            return (Float) o1 * (Float) o2;
        }
        else if(o1 instanceof Long && o2 instanceof Long) {
            return (Long) o1 * (Long) o2;
        }
        else if(o1 instanceof String && o2 instanceof String) {
            return ((String) o1).concat((String) o2);
        }
        else {
            return o1.hashCode() * o2.hashCode();
        }
    }

    /**
     * Gets the number boundary of given numberclass
     *
     * @param tClass The number class (e.g. Double.class)
     * @param min    The min
     * @param <T>    The type
     * @return The result
     */
    public static <T> T getBoundary(Class<T> tClass, boolean min) {
        if(!Number.class.isAssignableFrom(tClass)) return null;
        return (T) ReflectionUtil.getFieldObject(ReflectionUtil.getField(min ? "MIN_VALUE" : "MAX_VALUE", tClass), null);
    }

    /**
     * Converts a number to a hex value
     *
     * @param i  The value
     * @param r1 Should the string be fancy? (0xYYYY)
     * @param r0 Length of fancieness
     * @return The string
     */
    public static String toHex(long i, int r0, boolean r1) {
        String h = Long.toHexString(i).toUpperCase();
        while(h.length() < r0 && r1){
            h = 0 + h;
        }

        return (r1 ? "0x" : "") + h;
    }

    public static String toHex(long i) {
        return toHex(i, 2, false);
    }

    /**
     * Gets a random value between two values
     *
     * @param min The first value (the minimum)
     * @param max The second value (the maximum)
     * @return The result as int
     */
    public static int getRandom(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    /**
     * Rounds given number to n-places after the comma
     *
     * @param x The number
     * @param n The value (n)
     * @return The formatted string
     */
    public static String round(double x, int n) {
        DecimalFormat df = new DecimalFormat("#." + Strings.repeat("#", n));
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(x);
    }

}
