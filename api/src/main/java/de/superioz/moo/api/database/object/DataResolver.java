package de.superioz.moo.api.database.object;

import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;
import de.superioz.moo.api.keyvalue.TypeableKey;
import org.bson.Document;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for easier key resolving and appending/fetching from/on documents
 */
@Getter
public class DataResolver {

    private int index = 0;
    private Document doc;
    private DataArchitecture architecture;

    public DataResolver(DataArchitecture architecture) {
        this.architecture = architecture;
    }

    public DataResolver(Class<?> cl) {
        this(DataArchitecture.fromClass(cl));
    }

    /**
     * Sets the document
     *
     * @param doc The document
     * @return This
     */
    public DataResolver doc(Document doc) {
        this.doc = doc;
        return this;
    }

    public DataResolver doc(String name) {
        return doc((Document) doc.get(name));
    }

    /**
     * Gets the document
     *
     * @return The document
     */
    public Document document() {
        return doc;
    }

    /**
     * Resolves given instance into a document
     *
     * @param instance The instance
     * @return The bson document
     */
    public Document fullResolve(Object instance) {
        return doc(new Document()).appendAll(instance).document();
    }

    /**
     * Resolves index and increments it
     *
     * @return The type=key pair
     */
    private TypeableKey resolveNext() {
        return architecture.resolve(this.index++);
    }

    /**
     * Resets the index
     */
    public DataResolver resetIndex() {
        index = 0;
        return this;
    }

    /**
     * Completely convert document into given class object<br>
     * The document needs to be passed down with {@link #doc(Document)} beforehand
     *
     * @param clazz The class
     * @param <T>   The type
     * @return The result as T
     */
    public <T> T complete(Class<?> clazz) {
        if(doc == null) return null;
        T t = (T) ReflectionUtil.getInstance(clazz);

        TypeableKey pair;
        while((pair = resolveNext()) != null){
            String key = pair.getKey();
            Class<?> type = pair.getValueClass();

            Field f = getArchitecture().getClassFields().get(key);
            if(f == null || !f.getType().equals(type)) continue;

            // gets the object from the document
            // either over a sub-label (label.subPath) or directly (label)
            String[] s0 = key.split("\\.", 2);
            Object object;
            if(s0.length == 2) {
                object = ((Document) doc.get(s0[0])).get(s0[1]);
            }
            else {
                object = doc.get(key);
            }

            // try to change type of the object if it wouldn't fit
            if(object != null && !f.getType().isAssignableFrom(object.getClass())) {
                object = ReflectionUtil.safeCast(object.toString(), f);
            }

            ReflectionUtil.setFieldObject(f, t, object);
        }
        return t;
    }

    /**
     * Append complete data from class to {@link #doc}<br>
     * The nested document keys can only be up to layer two (e.g. player.textures)<br>
     * If {@link #doc} or {@code instance} is null than will be null returned
     *
     * @param instance The instance
     * @return This
     * @see #append(int, Object)
     * @see #append(Object[])
     * @see #appendSingle(String, Object)
     */
    public DataResolver appendAll(Object instance) {
        if(doc == null || instance == null) return null;
        Map<String, Document> documents = new LinkedHashMap<>();
        documents.put("", doc);

        TypeableKey pair;
        while((pair = resolveNext()) != null){
            String key = pair.getKey();
            Class<?> type = pair.getValueClass();

            Field f = getField(key, instance.getClass());
            if(f == null || !f.getType().equals(type) || !isResolvable(f)) continue;
            Object val = ReflectionUtil.getFieldObject(f, instance);

            String[] s0 = key.split("\\.", 2);
            if(s0.length == 2) {
                String document = s0[0];

                if(!documents.containsKey(document)) {
                    documents.put(document, new Document());
                }
                documents.get(document).append(s0[1], val);
            }
            else {
                documents.get("").append(key, val);
            }
        }

        for(String s : documents.keySet()) {
            if(!s.isEmpty()) {
                doc.append(s, documents.get(s));
            }
        }
        return this;
    }

    /**
     * Gets next object from the document
     *
     * @param <T> The type
     * @return The successful
     * @see #next(int, Object)
     * @see #next(Object)
     * @see #resolveNext()
     */
    @SuppressWarnings("unchecked")
    public <T> T next() {
        TypeableKey pair;
        if(doc == null || (pair = resolveNext()) == null) {
            return null;
        }

        String key = pair.getKey();
        Class<?> type = pair.getValueClass();

        Object o = doc.get(key);
        if(o == null || (type != null && !(type.isAssignableFrom(o.getClass())) && !o.getClass().isPrimitive())) {
            if(Number.class.isAssignableFrom(type) && (o != null && o instanceof Number)) {
                return (T) ReflectionUtil.safeCast(o + "", type);
            }
            return null;
        }
        return (T) o;
    }

    /**
     * Sets the next object to the obj
     *
     * @param obj The object
     */
    public void next(Object obj) {
        if(obj != null) {
            ReflectionUtil.setFieldObject(this.index, obj, next());
        }
    }

    /**
     * Sets next num objects to the obj
     *
     * @param num The amount of iterations
     * @param obj The object
     */
    public void next(int num, Object obj) {
        for(int i = 0; i < num; i++) {
            next(obj);
        }
    }

    /**
     * Appends given objects to document
     *
     * @param objects The objects
     * @param <T>     The type
     */
    @SafeVarargs
    public final <T> DataResolver append(T... objects) {
        if(doc != null) {
            for(T o : objects) {
                doc.append(resolveNext().getKey(), o == null ? null : o);
            }
        }
        return this;
    }

    /**
     * Appends only one object to the document
     *
     * @param key The key
     * @param o   The object
     * @param <T> The type
     * @return This
     */
    public <T> DataResolver appendSingle(String key, T o) {
        if(doc != null) {
            doc.append(key, o == null ? null : o);
        }
        return this;
    }

    /**
     * Appends num objects to the document
     *
     * @param num    The amount of iterations
     * @param object The object
     * @return This
     */
    public DataResolver append(int num, Object object) {
        for(int i = 0; i < num; i++) {
            String key = resolveNext().getKey();
            Object val = ReflectionUtil.getFieldObject(index - 1, object);
            doc.append(key, val);
        }
        return this;
    }

    /**
     * Gets a key from a field (through Dbkey.class)
     *
     * @param f The field
     * @return The key
     */
    public static String getKey(Field f) {
        if(!isResolvable(f)) return null;
        String name = f.getAnnotation(DbKey.class).key();
        if(name.isEmpty()) name = f.getName();
        return name;
    }

    /**
     * Checks if given field has the dbKey annotation
     *
     * @param f The field
     * @return The result
     */
    public static boolean isResolvable(Field f) {
        return f.isAnnotationPresent(DbKey.class);
    }

    /**
     * Gets a key from a field found with the id and the class
     *
     * @param id The id
     * @param c  The class
     * @return The key
     */
    public static String getKey(int id, Class<?> c) {
        return getKey(ReflectionUtil.getFieldFromId(id, c));
    }

    public static String getKey(String raw, Class<?> c) {
        return getKey(getField(raw, c));
    }

    /**
     * Gets the field where the dbKey-name is equals str
     *
     * @param str The string
     * @param c   The class to get the field from
     * @return The field
     */
    public static Field getField(String str, Class<?> c) {
        for(Field f : ReflectionUtil.getFields(c)) {
            if(str.equals(getKey(f))) {
                return f;
            }
        }
        return null;
    }

    /**
     * Get all fields from given class which is resolvable ({@link #isResolvable(Field)})
     *
     * @param c The class
     * @return The list of fields
     */
    public static List<Field> getResolvableFields(Class<?> c) {
        List<Field> l = new ArrayList<>();
        for(Field f : ReflectionUtil.getFields(c)) {
            if(isResolvable(f)) l.add(f);
        }
        return l;
    }

}
