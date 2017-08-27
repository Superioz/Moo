package de.superioz.moo.api.command.param;

import de.superioz.moo.api.command.CommandFlag;
import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.exceptions.InvalidArgumentException;
import de.superioz.moo.api.util.Operator;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Abstract class for wrapping a list of parameter.<br>
 * Some child classes are {@link ParamSet} or {@link CommandFlag} which uses this
 * class to nicely fetch values from the parameters
 */
public abstract class GenericParameterSet {

    /**
     * The pattern of the different parameter. Either splitted with ' ' or with '"'<br>
     * Example: '/command subcommand arg0 arg1 "arg2 arg2.1 arg2.3" arg4' results in these arguments:<br>
     * 'arg0', 'arg1', 'arg2 arg2.1 arg2.3', 'arg4'
     */
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("([\"][^\"]*[\"])|([^\" ]+)");

    /**
     * The list of parameters
     */
    @Getter @Setter
    private List<String> parameter;

    /**
     * The list of raw parameter
     */
    @Getter
    private List<String> rawParameter;

    /**
     * The parameter index to list parameter without using an explicit index<br>
     * Will be increased after every use
     */
    private int paramIndex = 0;

    protected GenericParameterSet(List<String> parameter) {
        this.rawParameter = parameter;
        this.parameter = parameter;
    }

    protected GenericParameterSet(String commandLine) {
        this(retrieveArguments(commandLine));
    }

    /**
     * Creates a new instance of the parameter set
     *
     * @param l The list of arguments
     * @return This
     */
    public static GenericParameterSet newInstance(List<String> l) {
        return new GenericParameterSet(l) {
        };
    }

    public static GenericParameterSet newInstance(String commandLine) {
        return new GenericParameterSet(commandLine) {
        };
    }

    /**
     * Gets all arguments from given command line with allowing '"' parts to be an argument group
     *
     * @param commandLine The commandline
     * @return The list of string
     */
    public static List<String> retrieveArguments(String commandLine, boolean mindEmpty) {
        List<String> l = StringUtil.getStringList(StringUtil.find(PARAMETER_PATTERN.pattern(), commandLine),
                s -> s.replaceAll("\"", ""));
        if((commandLine.endsWith(" ") && mindEmpty) || l.isEmpty()) l.add("");
        return l;
    }

    public static List<String> retrieveArguments(String commandLine) {
        return retrieveArguments(commandLine, false);
    }

    /**
     * Gets the arguments size
     *
     * @return The size as int
     */
    public int size() {
        return parameter.size();
    }

    /**
     * The raw command line
     *
     * @return The command line
     * @see #rawParameter
     */
    public String getRawCommandline() {
        return String.join(" ", getRawParameter());
    }

    /**
     * Get the raw parameter at given index (without validation)
     *
     * @param paramIndex The parameterIndex
     * @return The parameter
     */
    public String get(int paramIndex) {
        if(paramIndex >= parameter.size() || paramIndex < 0) return null;
        return parameter.get(paramIndex);
    }

    public String get() {
        return get(paramIndex);
    }

    /**
     * Get argument before the current
     *
     * @param shiftIndex The index to be shifted back
     * @return The argument as string
     */
    public String getBefore(int shiftIndex) {
        return get(size() - 1 - shiftIndex);
    }

    /**
     * Gets the next parameter
     *
     * @return The param
     */
    public String next() {
        String s = get(paramIndex);
        paramIndex++;
        return s;
    }

    public void resetParamIndex() {
        paramIndex = 0;
    }

    /**
     * Gets an argument range as string (e.g. for a message)
     *
     * @param paramIndex The parameterIndex
     * @param lastIndex  The last index
     * @return The string
     */
    public List<String> getRange(int paramIndex, int lastIndex) {
        return getParameter().subList(paramIndex, lastIndex);
    }

    public List<String> getRange(int paramIndex) {
        return getRange(paramIndex, size());
    }

    /**
     * Gets a parameter at given index with given class (with validation)
     *
     * @param paramIndex The parameter index
     * @param tClass     The type class
     * @param backupVal  The backup value (If it would return null or if the validation failed)
     * @param verifier   The verifier
     * @param <T>        The type
     * @return The parameter object
     */
    public <T> T get(int paramIndex, Class<T> tClass, T backupVal, Function<T, Boolean> verifier) {
        T t = get(paramIndex, tClass, backupVal);
        if(!verifier.apply(t)) {
            return backupVal;
        }
        return t;
    }

    public <T> T get(int paramIndex, Class<T> tClass, T backupVal, boolean condition) {
        return get(paramIndex, tClass, backupVal, f -> condition);
    }

    public <T> T get(int paramIndex, Class<T> tClass, Function<T, Boolean> verifier) {
        return get(paramIndex, tClass, null, verifier);
    }

    public <T> T get(int paramIndex, Class<T> tClass, boolean condition) {
        return get(paramIndex, tClass, null, f -> condition);
    }

    /**
     * Gets a parameter at given index with given class
     *
     * @param paramIndex The parameter index
     * @param tClass     The type class
     * @param backupVal  The backup value (if it would return null)
     * @param <T>        The type
     * @return The parameter object
     */
    public <T> T get(int paramIndex, Class<T> tClass, T backupVal) {
        if(paramIndex < 0) paramIndex = 0;

        ParamType<T> type = CommandRegistry.getInstance().getParamTypeRegistry().get(tClass);
        if(type == null) return backupVal;
        String param = get(paramIndex);

        T t;
        try {
            t = type.resolve(param);
        }
        catch(NumberFormatException ex) {
            t = null;
        }
        if(Number.class.isAssignableFrom(type.typeClass()) && t == null) {
            if(backupVal != null) return backupVal;
            throw new InvalidArgumentException(InvalidArgumentException.Type.CONVERT, param, tClass.getSimpleName());
        }

        return t;
    }

    public <T> T get(int paramIndex, Class<T> tClass) {
        return get(paramIndex, tClass, (T) null);
    }

    /**
     * Gets a string parameter at given index
     *
     * @param paramIndex The index
     * @param backupVal  Null = backupVal
     * @return The parameter
     */
    public String getString(int paramIndex, String backupVal) {
        return get(paramIndex, String.class, backupVal);
    }

    public String getString(int paramIndex, String backupVal, Function<String, Boolean> verifier) {
        return get(paramIndex, String.class, backupVal, verifier);
    }

    public String getString(int paramIndex, String backupVal, boolean condition) {
        return get(paramIndex, String.class, backupVal, condition);
    }

    public String getString(int paramIndex) {
        return get(paramIndex, String.class);
    }

    public String getString(int paramIndex, Function<String, Boolean> verifier) {
        return get(paramIndex, String.class, verifier);
    }

    public String getString(int paramIndex, boolean condition) {
        return get(paramIndex, String.class, condition);
    }

    /**
     * Gets an int parameter at given index
     *
     * @param paramIndex The index
     * @param backupVal  Null = backupVal
     * @return The parameter
     */
    public Integer getInt(int paramIndex, Integer backupVal) {
        return get(paramIndex, Integer.class, backupVal);
    }

    public Integer getInt(int paramIndex, Integer backupVal, Function<Integer, Boolean> verifier) {
        return get(paramIndex, Integer.class, backupVal, verifier);
    }

    public Integer getInt(int paramIndex, Integer backupVal, boolean condition) {
        return get(paramIndex, Integer.class, backupVal, condition);
    }

    public Integer getInt(int paramIndex) {
        return get(paramIndex, Integer.class);
    }

    public Integer getInt(int paramIndex, Function<Integer, Boolean> verifier) {
        return get(paramIndex, Integer.class, verifier);
    }

    public Integer getInt(int paramIndex, boolean condition) {
        return get(paramIndex, Integer.class, condition);
    }

    /**
     * Gets a long parameter at given index
     *
     * @param paramIndex The index
     * @param backupVal  Null = backupVal
     * @return The parameter
     */
    public Long getLong(int paramIndex, Long backupVal) {
        return get(paramIndex, Long.class, backupVal);
    }

    public Long getLong(int paramIndex, Long backupVal, Function<Long, Boolean> verifier) {
        return get(paramIndex, Long.class, backupVal, verifier);
    }

    public Long getLong(int paramIndex, Long backupVal, boolean condition) {
        return get(paramIndex, Long.class, backupVal, condition);
    }

    public Long getLong(int paramIndex) {
        return get(paramIndex, Long.class);
    }

    public Long getLong(int paramIndex, Function<Long, Boolean> verifier) {
        return get(paramIndex, Long.class, verifier);
    }

    public Long getLong(int paramIndex, boolean condition) {
        return get(paramIndex, Long.class, condition);
    }

    /**
     * Gets a double parameter at given index
     *
     * @param paramIndex The index
     * @param backupVal  Null = backupVal
     * @return The parameter
     */
    public Double getDouble(int paramIndex, Double backupVal) {
        return get(paramIndex, Double.class, backupVal);
    }

    public Double getDouble(int paramIndex, Double backupVal, Function<Double, Boolean> verifier) {
        return get(paramIndex, Double.class, backupVal, verifier);
    }

    public Double getDouble(int paramIndex, Double backupVal, boolean condition) {
        return get(paramIndex, Double.class, backupVal, condition);
    }

    public Double getDouble(int paramIndex) {
        return get(paramIndex, Double.class);
    }

    public Double getDouble(int paramIndex, Function<Double, Boolean> verifier) {
        return get(paramIndex, Double.class, verifier);
    }

    public Double getDouble(int paramIndex, boolean condition) {
        return get(paramIndex, Double.class, condition);
    }

    /**
     * Gets a boolean parameter at given index
     *
     * @param paramIndex The index
     * @param backupVal  Null = backupVal
     * @return The parameter
     */
    public Boolean getBoolean(int paramIndex, Boolean backupVal) {
        return get(paramIndex, Boolean.class, backupVal);
    }

    public Boolean getBoolean(int paramIndex, Boolean backupVal, Function<Boolean, Boolean> verifier) {
        return get(paramIndex, Boolean.class, backupVal, verifier);
    }

    public Boolean getDouble(int paramIndex, Boolean backupVal, boolean condition) {
        return get(paramIndex, Boolean.class, backupVal, condition);
    }

    public Boolean getBoolean(int paramIndex) {
        return get(paramIndex, Boolean.class);
    }

    public Boolean getBoolean(int paramIndex, Function<Boolean, Boolean> verifier) {
        return get(paramIndex, Boolean.class, verifier);
    }

    public Boolean getBoolean(int paramIndex, boolean condition) {
        return get(paramIndex, Boolean.class, condition);
    }

    /**
     * Gets an enum from given parameterIndex and class
     *
     * @param paramIndex The parameterIndex
     * @param enumClass  The enum class (e.g. {@link Operator})
     * @param backupVal  If something goes wrong ..
     * @param <E>        The enum type
     * @return The enum object
     */
    public <E extends Enum> E getEnum(int paramIndex, Class<E> enumClass, E backupVal) {
        String s = get(paramIndex);
        if(s == null) return backupVal;
        Enum[] enums = enumClass.getEnumConstants();

        // if the string is an integer list the enum directly
        if(Validation.INTEGER.matches(s)) {
            int i = Integer.parseInt(s);

            if(i >= enums.length || i < 0) return backupVal;
            return (E) enums[i];
        }

        for(Enum e : enums) {
            if(s.equalsIgnoreCase(e.name())) return (E) e;
        }
        return null;
    }

    public <E extends Enum> E getEnum(int paramIndex, Class<E> enumClass) {
        return getEnum(paramIndex, enumClass, (E) null);
    }

    public <E extends Enum> E getEnum(int paramIndex, Class<E> enumClass, E backupVal, Function<E, Boolean> verifier) {
        E e = getEnum(paramIndex, enumClass);
        if(e == null) return backupVal;
        return verifier.apply(e) ? e : backupVal;
    }

    public <E extends Enum> E getEnum(int paramIndex, Class<E> enumClass, E backupVal, boolean condition) {
        return getEnum(paramIndex, enumClass, backupVal, f -> condition);
    }

    public <E extends Enum> E getEnum(int paramIndex, Class<E> enumClass, Function<E, Boolean> verifier) {
        return getEnum(paramIndex, enumClass, null, verifier);
    }

    public <E extends Enum> E getEnum(int paramIndex, Class<E> enumClass, boolean condition) {
        return getEnum(paramIndex, enumClass, null, f -> condition);
    }

    @Override
    public String toString() {
        return parameter.toString();
    }
}
