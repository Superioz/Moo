package de.superioz.moo.api.database.query;

import de.superioz.moo.api.database.object.DataResolver;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.exceptions.CommandException;
import de.superioz.moo.api.keyvalue.KeyMultiValue;
import de.superioz.moo.api.keyvalue.Keyable;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.keyvalue.KeyValue;
import de.superioz.moo.api.util.Operator;
import de.superioz.moo.api.utils.NumberUtil;
import org.bson.Document;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A chain of different conditions, where the operator can be one of {@link DbQueryNode.Type}
 */
@Getter
@NoArgsConstructor
public class DbQuery {

    private static final Pattern GLOBAL_PATTERN = Pattern.compile("[^;]+(;[^;]*)*");
    private static final Pattern PATTERN = Pattern.compile("[^;]+");

    /**
     * Map of nodes (conditions)
     */
    private Map<String, DbQueryNode> nodes = new HashMap<>();

    /**
     * The class which holds the fields as keys
     */
    private Class<?> keyHoldingClass;

    public DbQuery(Class<?> keyHoldingClass) {
        this.keyHoldingClass = keyHoldingClass;
    }

    public DbQuery setKeyHoldingClass(Class<?> keyHoldingClass) {
        this.keyHoldingClass = keyHoldingClass;
        return this;
    }

    /**
     * Gets the size of the nodes map
     *
     * @return The size as int
     */
    public int getSize() {
        return nodes.size();
    }

    /**
     * Gets the dbQuery from a parameter list (represented as {@code msg})
     *
     * @param wrappedClass The class of the field keys (e.g. {@link PlayerData})
     * @param msg          The msg (e.g. "coins+50")
     * @return This
     * @throws CommandException If one part of the msg is wrong
     */
    public static DbQuery fromParameter(Class<?> wrappedClass, String msg) throws CommandException {
        // if the msg doesnt match the pattern - return null
        if(!GLOBAL_PATTERN.matcher(msg).matches()) {
            return null;
        }
        DbQuery query = new DbQuery(wrappedClass);

        // a map for storing all values for the DbQuery
        StringUtil.getByPattern(PATTERN, msg, s -> {
            DbQueryNode.Type t = null;
            for(DbQueryNode.Type type : DbQueryNode.Type.values()) {
                if(s.contains(type.toString())) {
                    t = type;
                }
            }
            return t;
        }, DbQueryNode.Type::toString, s -> Operator.AND, ",").forEach((stringTypePair, operatorListPair) -> {
            try {
                String key = stringTypePair.getKey();
                DbQueryNode.Type type = stringTypePair.getValue();
                DbModifier modifier = DbModifier.fromKey(key, wrappedClass);
                List<Integer> validations = modifier == null ? new ArrayList<>() : modifier.getValidationIds();

                List<String> values = operatorListPair.getValue();
                if(!values.isEmpty()) {
                    query.add(new DbQueryNode(key, type, validations, values.size() == 1 ? values.get(0) : values));
                }
            }
            catch(Exception ex) {
                //
            }
        });
        return query;
    }

    /**
     * Converts given object into a queryChain
     *
     * @param o The object
     * @return The new query chain
     */
    public static DbQuery fromObject(Object o) {
        DbQuery chain = new DbQuery(o.getClass());

        for(Field f : ReflectionUtil.getFieldsNonStatic(o.getClass())) {
            String key = DataResolver.getKey(f);
            Object val = ReflectionUtil.getFieldObject(f, o);

            DbQueryNode n = new DbQueryNode(key, DbQueryNode.Type.EQUATE, new ArrayList<>(),
                    val instanceof List ? (List) val : ReflectionUtil.getFieldObject(f, o));
            chain.add(n);
        }

        return chain;
    }

    /**
     * Clears all nodes
     */
    public DbQuery clear() {
        nodes.clear();
        return this;
    }

    /**
     * Applies values to the object
     *
     * @param instance     The object
     * @param skipEquality Should the field be skipped if it's equals to the value?
     * @param raw          Just setting the value or note the operator?
     * @return The object with applied values
     */
    public Integer apply(Object instance, boolean skipEquality, boolean raw) {
        int skipped = 0;

        for(String s : nodes.keySet()) {
            DbQueryNode node = nodes.get(s);
            Object o = node.getValue();
            Field f = DataResolver.getField(s, instance.getClass());
            if(f == null) continue;

            if(skipEquality) {
                Object o2 = ReflectionUtil.getFieldObject(f, instance);

                if(o.equals(o2)) {
                    skipped++;
                    continue;
                }
            }

            if(raw) {
                node.rawApply(instance);
            }
            else {
                node.apply(instance);
            }
        }
        return skipped;
    }

    public void apply(Object instance) {
        this.apply(instance, false, false);
    }

    public void applyRaw(Object instance) {
        this.apply(instance, true, true);
    }

    /**
     * Appends a node to the list
     *
     * @param node The node
     * @return This
     */
    public DbQuery add(DbQueryNode node) {
        this.nodes.put(node.getKey(), node);
        return this;
    }

    public DbQuery add(DbModifier key, DbQueryNode.Type type, Object object) {
        String k = DataResolver.getKey(key.getId(), getKeyHoldingClass());
        Field f = DataResolver.getField(k, getKeyHoldingClass());

        if(f != null && f.getType().isAssignableFrom(List.class) && !(object instanceof List)) {
            List l = new ArrayList();
            l.add(object);
            object = l;
        }
        return this.add(new DbQueryNode(k, type, key.getValidationIds(), object));
    }

    public DbQuery add(String key, DbQueryNode.Type type, Object object) {
        return this.add(new DbQueryNode(key, type, new ArrayList<>(), object));
    }

    /**
     * Similar to {@link #add(DbModifier, DbQueryNode.Type, Object)} but without passing type over
     *
     * @param key   The key
     * @param value The value
     * @return This
     */
    public DbQuery equate(DbModifier key, Object value) {
        return add(key, DbQueryNode.Type.EQUATE, value);
    }

    public DbQuery equate(String key, Object value) {
        return add(key, DbQueryNode.Type.EQUATE, value);
    }

    /**
     * Similar to {@link #add(DbModifier, DbQueryNode.Type, Object)} but without passing type over
     *
     * @param key   The key
     * @param value The value
     * @return This
     */
    public DbQuery append(DbModifier key, Object value) {
        return add(key, DbQueryNode.Type.APPEND, value);
    }

    public DbQuery append(String key, Object value) {
        return add(key, DbQueryNode.Type.APPEND, value);
    }

    /**
     * Similar to {@link #add(DbModifier, DbQueryNode.Type, Object)} but without passing type over
     *
     * @param key   The key
     * @param value The value
     * @return This
     */
    public DbQuery subtract(DbModifier key, Object value) {
        return add(key, DbQueryNode.Type.SUBTRACT, value);
    }

    public DbQuery subtract(String key, Object value) {
        return add(key, DbQueryNode.Type.SUBTRACT, value);
    }

    /**
     * Clears object from given key
     *
     * @param key The key
     * @return This
     */
    public DbQuery clear(String key) {
        this.nodes.remove(key);
        return this;
    }

    /**
     * Gets the object with the key
     *
     * @param keyIndex The keyIndex
     * @return The value
     */
    public DbQueryNode get(int keyIndex) {
        return this.nodes.get(DataResolver.getKey(keyIndex, getKeyHoldingClass()));
    }

    public boolean has(int keyIndex) {
        return get(keyIndex) != null;
    }

    /**
     * Validate this db query object
     *
     * @return The result
     */
    public boolean validate() {
        boolean r = true;
        for(DbQueryNode n : getNodes()) {
            r = r && n.validateValues();
        }
        return r;
    }

    /**
     * Get all nodes
     *
     * @return The list of nodes
     */
    public List<DbQueryNode> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    /**
     * Builds this query to a document through {@link MongoQuery}
     *
     * @return The document
     */
    public Document toDocument() {
        return toMongoQuery().build();
    }

    /**
     * Turns this query chain into a mongoQuery
     *
     * @return The mongoQuery
     */
    public MongoQuery toMongoQuery(Object instance) {
        MongoQuery query = new MongoQuery();

        for(String s : nodes.keySet()) {
            DbQueryNode node = nodes.get(s);
            Field field = DataResolver.getField(s, getKeyHoldingClass());
            if(field == null) continue;
            Object fieldObject = instance != null ? ReflectionUtil.getFieldObject(field, instance) : null;

            MongoQuery.Operator operator = null;
            Keyable keyable = null;

            switch(node.getType()) {
                case EQUATE:
                    operator = MongoQuery.Operator.SET;
                    keyable = new KeyValue(s, node.getValue());
                    break;
                case APPEND:
                    if(field.getType().isAssignableFrom(List.class)) {
                        Object value = node.getValue();
                        if(!(value instanceof List)) {
                            value = new ArrayList(Collections.singletonList(value));
                        }
                        if(fieldObject != null) {
                            value = new Document("$each", value);
                        }

                        operator = MongoQuery.Operator.ADD_TO_SET;
                        keyable = new KeyValue(s, value);
                    }
                    else if(Number.class.isAssignableFrom(field.getType())) {
                        operator = MongoQuery.Operator.INC;
                        keyable = new KeyMultiValue(s, node.getValue());
                    }
                    else {
                        operator = MongoQuery.Operator.SET;
                        keyable = new KeyValue(s, node.getValue());
                    }
                    break;
                case SUBTRACT:
                    if(List.class.isAssignableFrom(field.getType())) {
                        Object value = node.getValue();
                        if(!(value instanceof List)) {
                            value = new ArrayList(Collections.singletonList(value));
                        }
                        operator = MongoQuery.Operator.PULL_ALL;
                        keyable = new KeyValue(s, value);
                    }
                    else if(Number.class.isAssignableFrom(field.getType())) {
                        Object object = node.getValue();
                        object = NumberUtil.multiply(object, -1);

                        operator = MongoQuery.Operator.INC;
                        keyable = new KeyMultiValue(s, object);
                    }
                    else {
                        operator = MongoQuery.Operator.SET;
                        keyable = new KeyValue(s, node.getValue());
                    }
                    break;
            }

            // try to fix null values
            if(fieldObject == null
                    && instance != null) {
                operator = MongoQuery.Operator.SET;
            }
            query.append(operator, keyable);
        }
        return query;
    }

    public MongoQuery toMongoQuery() {
        return toMongoQuery(null);
    }

    /**
     * Turns a stringList into this
     *
     * @param objects The objects
     * @return A new query chain
     */
    public static DbQuery fromStringList(List<String> objects) {
        if(objects.isEmpty()) {
            return null;
        }

        DbQuery chain = new DbQuery();
        for(String s : objects) {
            try {
                DbQueryNode n = DbQueryNode.fromString(s);
                chain.add(n);
            }
            catch(Exception e) {
                e.printStackTrace();
                //
            }
        }
        return chain;
    }

    /**
     * Turns the chain into a stringList
     *
     * @return The list of strings (nodes#toString)
     */
    public List<String> toStringList() {
        List<String> pairs = new ArrayList<>();
        for(String key : nodes.keySet()) {
            pairs.add(nodes.get(key).toString());
        }
        return pairs;
    }

    @Override
    public String toString() {
        List<String> l = new ArrayList<>();

        for(String key : nodes.keySet()) {
            DbQueryNode n = nodes.get(key);
            l.add(key + n.getType().toString() + "'" + n.getValue() + "'");
        }
        return l.toString();
    }

}
