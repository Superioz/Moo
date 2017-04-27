package de.superioz.moo.api.database;

import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;
import de.superioz.moo.api.utils.NumberUtil;
import de.superioz.moo.api.util.Validation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Just one node of the dbQuery {@link DbQuery}
 */
@Getter
public class DbQueryNode {

    /**
     * Key of the node
     */
    private String key;

    /**
     * Query type of the node
     */
    private Type type;

    /**
     * Validation for the value
     */
    private List<Integer> validationIds;

    /**
     * The value
     */
    private Object value;

    public DbQueryNode(String key, Type type, List<Integer> validationIds, Object value) {
        this.key = key;
        this.type = type;
        this.validationIds = validationIds;
        this.value = value;
    }

    /**
     * Applies this node onto the instance
     *
     * @param instance The instance
     */
    public void apply(Object instance) {
        if(!validateValues()) return;

        Field field = ReflectionUtil.getField(key, instance.getClass());
        Object fieldObject = ReflectionUtil.getFieldObject(field, instance);
        Object value = getValue() instanceof List ? getValue() : ReflectionUtil.safeCast(getValue() + "", field);

        // if the type is not equate
        if(type != Type.EQUATE) {
            if(field != null && List.class.isAssignableFrom(field.getType())) {
                List l = (List) fieldObject;
                if(fieldObject == null) l = new ArrayList<>();
                List<Object> values = new ArrayList<>();
                if(value instanceof List) values = (List) value;
                else values.add(value);

                for(Object val : values) {
                    if(type == Type.APPEND) {
                        if(!l.contains(val)) l.add(val);
                    }
                    else {
                        l.remove(val);
                    }
                }
                fieldObject = l;
            }
            else {
                if(!NumberUtil.checkBounds(fieldObject, value, type == Type.APPEND)) {
                    this.value = (value = value != null ? NumberUtil.getBoundary(value.getClass(), type == Type.SUBTRACT) : null);
                    this.type = Type.EQUATE;
                }

                fieldObject = type == Type.APPEND ? NumberUtil.append(fieldObject, value)
                        : type == Type.SUBTRACT ? NumberUtil.subtract(fieldObject, value)
                        : value;
            }
        }
        else {
            fieldObject = value;
        }

        ReflectionUtil.setFieldObject(field, instance, fieldObject);
    }

    public void rawApply(Object instance) {
        Field field = DataResolver.getField(key, instance.getClass());
        Object val = getValue();

        ReflectionUtil.setFieldObject(field, instance, val instanceof List ? val : ReflectionUtil.safeCast(val + "", field));
    }

    /**
     * Validate all values
     *
     * @return The result
     */
    public boolean validateValues() {
        boolean r = true;
        List<Validation> validations = getValidations();

        for(Validation v : validations) {
            if(value instanceof List) {
                List l = (List) value;

                for(Object o : l) {
                    r = v.matches(o + "");
                }
            }
            else {
                r = v.matches(value + "");
            }

            if(!r) break;
        }
        return r;
    }

    /**
     * Get all validation objects from validationIds
     *
     * @return The list of validations
     */
    public List<Validation> getValidations() {
        List<Validation> validations = new ArrayList<>();

        for(int i : validationIds) {
            validations.add(Validation.from(i));
        }

        return validations;
    }

    /**
     * Turns given string into a dbQueryNode object
     *
     * @param s The string
     * @return The queryNode
     */
    public static DbQueryNode fromString(String s) throws Exception {
        List<String> l = StringUtil.split(s, StringUtil.SEPERATOR);

        String key = l.get(0);
        Type type = Type.values()[Integer.valueOf(l.get(1))];

        List<Integer> validationIds = new ArrayList<>();
        List<String> l0 = StringUtil.split(l.get(2), ";");
        if(!l0.isEmpty() && !l0.get(0).isEmpty()) {
            for(String s0 : l0) {
                validationIds.add(Integer.valueOf(s0));
            }
        }
        Object value = ReflectionUtil.safeCast(l.get(3));

        return new DbQueryNode(key, type, validationIds, value);
    }

    @Override
    public String toString() {
        List<Object> l = new ArrayList<>();
        l.add(key);
        l.add(type.ordinal());
        l.add(StringUtil.join(";", validationIds.toArray(new Object[]{})));
        l.add(value);

        return StringUtil.join(StringUtil.SEPERATOR, l.toArray(new Object[]{}));
    }

    public enum Type {

        EQUATE("="),
        APPEND("+"),
        SUBTRACT("-"),
        UNKNOWN("?");

        private String symbol;

        Type(String s) {
            this.symbol = s;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

}
