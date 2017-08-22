package de.superioz.moo.api.database.filter;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import de.superioz.moo.api.database.object.DataResolver;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.exceptions.InvalidArgumentException;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import javafx.util.Pair;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.util.Operator;
import de.superioz.moo.api.util.Validation;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Represents a filter to search for occurences inside the {@link DatabaseCollection}<br>
 * That means inside the database AND the cache
 */
@Getter
@NoArgsConstructor
public class DbFilter {

    private static final Pattern GLOBAL_PATTERN = Pattern.compile("[^;]+(;[^;]*)*");
    private static final Pattern PATTERN = Pattern.compile("[^;]+");
    private static final Pattern NO_OPERATION = Pattern.compile("^[^=!<>]*$");

    /**
     * Root node of the dbFilter map
     */
    private DbFilterNode root;

    /**
     * All nodes as set
     */
    private Set<DbFilterNode> nodes = new HashSet<>();

    public DbFilter(Bson bson) {
        readObject(bson.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry()).toJson()).replaceStandardUniqueIds();
    }

    /**
     * Gets a dbFilter from given string message (Syntax of {@link #GLOBAL_PATTERN}
     *
     * @param wrappedClass The wrapped class for eventually a prim key (only for this purpose)
     * @param msg          The message after global syntax
     * @return A new filter instance
     * @throws InvalidArgumentException If one part of the syntax is wrong (message is the part itself)
     */
    public static DbFilter fromParameter(Class<?> wrappedClass, String msg) throws InvalidArgumentException {
        if(!GLOBAL_PATTERN.matcher(msg).matches()) {
            return null;
        }
        if(NO_OPERATION.matcher(msg).matches()) {
            if(wrappedClass == null) return null;
            else return DbFilter.fromPrimKey(wrappedClass, msg);
        }
        List<Bson> filtersList = new ArrayList<>();

        // the map of contents for msg
        Map<Pair<String, Operator>, Pair<Operator, List<String>>> map =
                StringUtil.getByPattern(PATTERN, msg, s -> {
                    Operator op = null;
                    for(Operator operator : Operator.values()) {
                        if(operator == Operator.AND || operator == Operator.OR
                                || operator == Operator.UNKNOWN) continue;
                        if(s.contains(operator.getSymbol())) {
                            op = operator;
                        }
                    }
                    return op;
                }, Operator::getSymbol, s -> {
                    Operator valueOperator = Operator.AND;
                    if(s.contains("|")) {
                        valueOperator = Operator.OR;
                    }
                    return valueOperator;
                }, ",|\\|");

        map.forEach((pair, pair0) -> {
            String key = pair.getKey();
            Operator operator0 = pair.getValue();
            Operator operator1 = pair0.getKey();
            List<String> values = pair0.getValue();

            List<Bson> f = new ArrayList<>();
            for(String s : values) {
                Method method = ReflectionUtil.getMethod(Filters.class, operator0.getShortcut());
                Bson fObject = (Bson) ReflectionUtil.invokeMethod(method, null, key, ReflectionUtil.safeCast(s));
                f.add(fObject);
            }

            filtersList.add(operator1 == Operator.OR ? Filters.or(f) : Filters.and(f));
        });
        if(filtersList.isEmpty()) return null;
        Bson bson = Filters.and(filtersList);

        return new DbFilter(bson);
    }

    /**
     * Gets a DbFilter object from a key object
     *
     * @param c     The class
     * @param index The index of the key
     * @param value The object of the key
     * @return This
     */
    public static DbFilter fromKey(Class<?> c, int index, Object value) {
        return new DbFilter(Filters.eq(DataResolver.getKey(index, c), value));
    }

    public static DbFilter fromPrimKey(Class<?> c, Object value) {
        return fromKey(c, 0, value);
    }

    public static DbFilter fromObjectsPrimKey(Class<?> c, Object instance) {
        return fromPrimKey(c, ReflectionUtil.getFieldObject(0, instance));
    }

    /**
     * Convert all placeHolder with numbers to field names from given class
     *
     * @param keyHoldingClass The class
     */
    public DbFilter convert(Class<?> keyHoldingClass) {
        String json = root.getValue().toString();

        for(String s : StringUtil.find("\"[0-9*]\" : ", json)) {
            int i = Integer.parseInt(s.replace("\"", "").replace(": ", "").replace(" ", ""));
            json = json.replaceAll("\"" + i + "\" : ",
                    "\"" + ReflectionUtil.getFieldFromId(i, keyHoldingClass).getName() + "\" : ");
        }
        return readObject(new JSONObject(json));
    }

    /**
     * Sets the root of the structure
     *
     * @param root The root
     */
    private void setRoot(DbFilterNode root) {
        this.root = root;
        this.addNode(root);
    }

    /**
     * Adds a node to the structure
     *
     * @param node The node
     * @return This
     */
    private DbFilter addNode(DbFilterNode node) {
        this.nodes.add(node);
        return this;
    }

    /**
     * Get the size of this tree (leafs)
     *
     * @return The result as int
     */
    public int getSize() {
        return getLeafs().size();
    }

    /**
     * Returns all leafs from this tree structure
     *
     * @return The list of nodes which are leafs
     */
    public List<DbFilterNode> getLeafs() {
        List<DbFilterNode> leafs = new ArrayList<>();
        for(DbFilterNode node : nodes) {
            if(node.isLeaf()) leafs.add(node);
        }
        return leafs;
    }

    /**
     * Gets a leaf with given key
     *
     * @param key The key
     * @return A leaf with given key
     */
    public DbFilterNode getLeaf(String key) {
        for(DbFilterNode node : getLeafs()) {
            if(node.getNodeKey().equals(key)) return node;
        }
        return null;
    }

    /**
     * Checks if getting the key is not null
     *
     * @param index          The index of the field's name
     * @param keyHolderClass The keyHolderClass
     * @return The result
     */
    public boolean hasKey(int index, Class<?> keyHolderClass) {
        return getKey(index, keyHolderClass) != null;
    }

    /**
     * Gets a key (leaf from the structure) with given name of the field #index from keyHolderClass
     *
     * @param index          The index of the field
     * @param keyHolderClass The keyHolderClass
     * @return The node/leaf
     */
    public DbFilterNode getKey(int index, Class<?> keyHolderClass) {
        return getLeaf(ReflectionUtil.getFieldFromId(index, keyHolderClass).getName());
    }

    /**
     * Resolves the root
     *
     * @param onCondition The condition
     * @return The result
     */
    public boolean resolve(Function<DbFilterNode, Boolean> onCondition) {
        return root.resolve(onCondition);
    }

    /**
     * Reads json object into a conditional tree
     *
     * @param object The json object
     * @return This
     */
    public DbFilter readObject(JSONObject object) {
        DbFilterNode root = new DbFilterNode("$", object, "$");
        this.nodes.clear();
        this.setRoot(root);
        return readObject(root, "$");
    }

    public DbFilter readObject(String json) {
        return readObject(new JSONObject(json));
    }

    private DbFilter readObject(DbFilterNode prev, String path) {
        Object o = prev.getValue();
        String parentPath = path;

        if(o instanceof JSONObject) {
            JSONObject object = (JSONObject) o;

            for(String key : object.keySet()) {
                Object value = object.get(key);
                //if(Validation.UNIQUEID.matches(value + "")) value = UUID.fromString(value + "");
                path = parentPath + "." + key;

                DbFilterNode node = new DbFilterNode(key, value, path);
                node.setParent(prev);
                prev.addChildren(node);

                if(value instanceof JSONObject || value instanceof JSONArray) {
                    this.addNode(node);
                    this.readObject(node, path);
                }
                else {
                    // is a value
                    this.addNode(node);
                    //System.out.println("- VALUE: " + value + "[:" + key + "](Parent: " + label + ")");
                }
            }
        }
        else {
            JSONArray object = (JSONArray) o;

            object.forEach(o1 -> {
                if((o1 + "").equalsIgnoreCase("null")) {
                    o1 = null;
                }

                prev.setValue(o1);
                this.readObject(prev, parentPath);
            });
        }
        return this;
    }

    /**
     * Compares given objects with given operator
     *
     * @param o1       The first object
     * @param operator The operator
     * @param o2       The second object
     * @return The result
     */
    private boolean compare(Object o1, Operator operator, Object o2) {
        switch(operator) {
            case EQUALS:
                return o1.equals(o2);
            case NOT_EQUALS:
                return !o1.equals(o2);
            case GREATER_THAN:
                return o1 instanceof Comparable && o2 instanceof Comparable && ((Comparable) o1).compareTo(o2) > 0;
            case GREATER_THAN_OR_EQUALS:
                return o1 instanceof Comparable && o2 instanceof Comparable && ((Comparable) o1).compareTo(o2) >= 0;
            case LESS_THAN:
                return o1 instanceof Comparable && o2 instanceof Comparable && ((Comparable) o1).compareTo(o2) < 0;
            case LESS_THAN_OR_EQUALS:
                return o1 instanceof Comparable && o2 instanceof Comparable && ((Comparable) o1).compareTo(o2) <= 0;
        }
        return false;
    }

    /**
     * Replace standard uuid with binary uuid<br>
     * This is needed because MongoDB sucks and I have to use the BSON format somehow, that's why
     * uuids have to be converted to binary uuids and vice versa
     *
     * @return This
     */
    public DbFilter replaceStandardUniqueIds() {
        String json = toString();

        for(String s : StringUtil.find("\\{\"[0-9a-z]*\":\"(" + Validation.UNIQUEID.getRawRegex() + ")\"}", json)) {
            String[] s0 = s.split(":");
            String key = s0[0].replace("\"", "").replace("{", "");
            UUID uuid = UUID.fromString(s0[1].replace("\"", "").replace("}", ""));

            String obj = "{\"" + key + "\":{" + convertStandardUUID(uuid) + "}}";
            json = json.replace(s, obj);
        }
        return readObject(json);
    }

    /**
     * Replace binary uuid with standard uuid<br>
     * This is needed because MongoDB sucks and I have to use the BSON format somehow, that's why
     * uuids have to be converted to binary uuids and vice versa
     *
     * @return This
     */
    public DbFilter replaceBinaryUniqueIds() {
        String json = toString();

        for(String s : StringUtil.find(Validation.BINARY_UNIQUEID_JSON.getRegex(), json)) {
            String key = s.split(":")[0].replace("{", "").replace("\"", "").replace("\\\"", "");
            UUID uuid = convertBinaryUUID(key, s);
            JSONObject object = new JSONObject().put(key, uuid);

            json = json.replace(s, object.toString());
        }
        return new DbFilter().readObject(json);
    }

    /**
     * Converts the standard uuid to a binary uuid
     *
     * @param uuid The uuid
     * @return The uuid
     */
    private String convertStandardUUID(UUID uuid) {
        String json = Filters.eq("", uuid).toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry()).toJson();
        json = json.split(":", 2)[1].replace(" ", "").replace("{", "").replace("}", "");

        return json;
    }

    /**
     * Converts a binary uuid to a standard uuid
     *
     * @param json The json string (should contain "$binary" and "$type" or something like that)
     * @return The uuid
     */
    private UUID convertBinaryUUID(String key, String json) {
        BsonDocument document = BsonDocument.parse(json);

        BsonValue value = document.get(key);
        if(!(value instanceof BsonBinary)) {
            return null;
        }
        byte[] bytes = ((BsonBinary) value).getData();
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        long l1 = bb.getLong();
        long l2 = bb.getLong();

        return new UUID(l1, l2);
    }

    /**
     * Turns the json structure into bson
     *
     * @return The bson
     */
    public Bson toBson() {
        return Document.parse(toString());
    }

    /**
     * Turns the json structure into a predicate
     *
     * @param <T> A type
     * @return The predicate of T
     */
    public <T> Predicate<T> toPredicate() {
        return t -> {
            Class<?> instanceClass = t.getClass();

            return resolve(node -> {
                try {
                    Field f = ReflectionUtil.getField(node.getNodeKey(), instanceClass);
                    Object o1 = ReflectionUtil.getFieldObject(f, t);

                    return compare(o1, node.getOperator(), node.getContent());
                }
                catch(Exception e) {
                    System.err.println("Error while converting db filter into predicate: " + e.getCause());
                }
                return false;
            });
        };
    }

    public String toString(int intent) {
        return ((JSONObject) getRoot().getValue()).toString(intent);
    }

    @Override
    public String toString() {
        return toString(0);
    }

}
