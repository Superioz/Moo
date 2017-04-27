package de.superioz.moo.api.database;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.keyvalue.Triple;
import de.superioz.moo.api.util.Validation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for defining a {@link DbQuery} without the need to pass over a wrapping class<br>
 * This will be done after this class has been used by another method ({@link #bake(Class)})
 *
 * @see DbQuery
 * @see DbQueryNode
 * @see DbModifier
 * @see DatabaseType
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DbQueryUnbaked {

    private List<Triple<Object, DbModifier, DbQueryNode.Type>> modifiers = new ArrayList<>();
    private List<DbQueryNode> rawModifiers = new ArrayList<>();

    /**
     * Creates a new instance of this unbaked {@link DbQuery}<br>
     * If you want to directly add a modifer use either {@link #newInstance(DbModifier, Object)} or
     * {@link #newInstance(String, Object, Validation...)}
     *
     * @return This
     */
    public static DbQueryUnbaked newInstance() {
        return new DbQueryUnbaked();
    }

    public static DbQueryUnbaked newInstance(DbModifier modifier, Object object) {
        return newInstance().equate(modifier, object);
    }

    public static DbQueryUnbaked newInstance(String key, Object object, Validation... validations) {
        return newInstance().equate(key, object, validations);
    }

    /**
     * Adds a new modifier to the modifiers list
     *
     * @param modifier The modifier to get the field id and validation from
     * @param type     The type of operation
     * @param object   The object to apply the operation to
     * @return This
     */
    public DbQueryUnbaked add(DbModifier modifier, DbQueryNode.Type type, Object object) {
        modifiers.add(new Triple<>(object, modifier, type));
        return this;
    }

    public DbQueryUnbaked add(DbQueryNode node) {
        rawModifiers.add(node);
        return this;
    }

    /**
     * Similar to {@link #add(DbModifier, DbQueryNode.Type, Object)} but with raw key (if you define a custom database)
     *
     * @param key           The key of the field
     * @param type          The type of operation
     * @param value         The object to apply the operation to
     * @param validationIds The ids of the value validation
     * @return This
     */
    public DbQueryUnbaked add(String key, DbQueryNode.Type type, Object value, List<Integer> validationIds) {
        return add(new DbQueryNode(key, type, validationIds, value));
    }

    public DbQueryUnbaked add(String key, DbQueryNode.Type type, Object value, Validation... validations) {
        List<Integer> validationIds = new ArrayList<>();
        for(Validation validation : validations) {
            validationIds.add(validation.ordinal());
        }
        return add(new DbQueryNode(key, type, validationIds, value));
    }

    /**
     * Adds a modifier where the object will be equated to the field
     *
     * @param modifier The modifier
     * @param object   The value
     * @return This
     */
    public DbQueryUnbaked equate(DbModifier modifier, Object object) {
        return add(modifier, DbQueryNode.Type.EQUATE, object);
    }

    public DbQueryUnbaked equate(String key, Object object, Validation... validations) {
        return add(key, DbQueryNode.Type.EQUATE, object, validations);
    }

    /**
     * Adds a modifier where the object will be appended to the field
     *
     * @param modifier The modifier
     * @param object   The value
     * @return This
     */
    public DbQueryUnbaked append(DbModifier modifier, Object object) {
        return add(modifier, DbQueryNode.Type.APPEND, object);
    }

    public DbQueryUnbaked append(String key, Object object, Validation... validations) {
        return add(key, DbQueryNode.Type.APPEND, object, validations);
    }

    /**
     * Adds a modifier where the object will be subtracted from the field
     *
     * @param modifier The modifier
     * @param object   The value
     * @return This
     */
    public DbQueryUnbaked subtract(DbModifier modifier, Object object) {
        return add(modifier, DbQueryNode.Type.SUBTRACT, object);
    }

    public DbQueryUnbaked subtract(String key, Object object, Validation... validations) {
        return add(key, DbQueryNode.Type.SUBTRACT, object, validations);
    }

    /**
     * Bakes this query that means passing over a wrapped class to append all modifiers to
     * a 'real' {@link DbQuery}
     *
     * @param wrappedClass The class of the to-edit object (e.g. Player.class, ...)
     * @return This as baked DbQuery
     */
    public DbQuery bake(Class<?> wrappedClass) {
        DbQuery query = new DbQuery(wrappedClass);

        for(Triple<Object, DbModifier, DbQueryNode.Type> modifier : modifiers) {
            Object object = modifier.getA();
            DbModifier modifyType = modifier.getB();
            DbQueryNode.Type type = modifier.getC();

            if(type == DbQueryNode.Type.EQUATE) {
                query.equate(modifyType, object);
            }
            else if(type == DbQueryNode.Type.APPEND) {
                query.append(modifyType, object);
            }
            else {
                query.subtract(modifyType, object);
            }
        }
        for(DbQueryNode node : rawModifiers) {
            query.add(node);
        }

        return query;
    }

}
