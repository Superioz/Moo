package de.superioz.moo.api.database.object;

import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import de.superioz.moo.api.keyvalue.TypeableKey;
import org.bson.Document;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents data from an object, that means that every field will be listed as a typeableKey<br>
 * It also makes use of the {@link DataResolver} class to convert a {@link Document} into an object
 * and vice versa.
 */
public class DataArchitecture {

    /**
     * The type=key pairs
     */
    @Getter
    private final List<TypeableKey> types;

    /**
     * The map of key=field pairs
     */
    @Getter
    private final Map<String, Field> classFields;

    /**
     * The class this architecture wraps in
     */
    @Getter @Setter
    private Class<?> wrappedClass;

    private DataArchitecture(Map<String, Field> fields, TypeableKey... types) {
        this.classFields = fields;
        this.types = Arrays.asList(types);
    }

    /**
     * Parses given class into a dataArchitecture
     *
     * @param clazz The clazz
     * @return The architecture
     */
    public static DataArchitecture fromClass(Class<?> clazz) {
        List<TypeableKey> types = new ArrayList<>();
        Map<String, Field> fieldMap = new HashMap<>();

        for(Field f : ReflectionUtil.getFieldsNonStatic(clazz)) {
            if(DataResolver.isResolvable(f)) {
                types.add(new TypeableKey(DataResolver.getKey(f), f.getType()));
            }
            fieldMap.put(f.getName(), f);
        }

        // sort the typeable key (looks nicer inside mongodb)
        types.sort((o1, o2) -> o2.getKey().compareTo(o1.getKey()) * -1);

        DataArchitecture architecture = new DataArchitecture(fieldMap, types.toArray(new TypeableKey[]{}));
        architecture.setWrappedClass(clazz);
        return architecture;
    }

    /**
     * Gets a key forward given index and type
     *
     * @param index The index
     * @param c     The class (type)
     * @param <T>   The type of class
     * @return The key
     */
    public <T> String resolve(int index, Class<T> c) {
        List<TypeableKey> l = types.stream().filter(akv -> akv.getValueClass().equals(c)).collect(Collectors.toList());

        if(index > (l.size() - 1) || index < 0) {
            return null;
        }
        return l.get(index).getKey();
    }

    public TypeableKey resolve(int index) {
        if(index > types.size() - 1 || index < 0) return null;
        return types.get(index);
    }

    @Override
    public String toString() {
        List<String> l = new ArrayList<>();
        for(TypeableKey key : types) {
            l.add(key.getKey() + "(" + key.getValueClass().getSimpleName() + ")");
        }

        return wrappedClass.getSimpleName() + "{" + l + "}";
    }
}
