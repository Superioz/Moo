package de.superioz.moo.api.database;

import de.superioz.moo.api.database.object.DataResolver;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum DbModifier {

    // ban
    BAN_BANNED(0, Ban.class),
    BAN_BY(1, Ban.class),
    BAN_START(2, Ban.class),
    BAN_DURATION(3, Ban.class),
    BAN_REASON(4, Ban.class),
    BAN_NOTES(5, Ban.class),

    // group
    GROUP_NAME(0, Group.class, Validation.SIMPLE_NAME),
    GROUP_RANK(1, Group.class, Validation.INTEGER),
    GROUP_PERMISSIONS(2, Group.class, Validation.PERMISSION),
    GROUP_INHERITANCES(3, Group.class, Validation.SIMPLE_NAME),
    GROUP_PREFIX(4, Group.class, Validation.NORMAL_STRING),
    GROUP_SUFFIX(5, Group.class, Validation.NORMAL_STRING),
    GROUP_COLOR(6, Group.class, Validation.COLOR),
    GROUP_TAB_PREFIX(7, Group.class, Validation.NORMAL_STRING),
    GROUP_TAB_SUFFIX(8, Group.class, Validation.NORMAL_STRING),

    // playerData
    PLAYER_UUID(0, PlayerData.class),
    PLAYER_NAME(1, PlayerData.class, Validation.NORMAL_STRING),
    PLAYER_IP(2, PlayerData.class),
    PLAYER_GROUP(3, PlayerData.class, Validation.SIMPLE_NAME),
    PLAYER_RANK(4, PlayerData.class, Validation.INTEGER),
    PLAYER_SERVER(5, PlayerData.class, Validation.NORMAL_STRING),
    PLAYER_PROXY(6, PlayerData.class, Validation.INTEGER),
    PLAYER_LAST_ONLINE(7, PlayerData.class),
    PLAYER_FIRST_ONLINE(8, PlayerData.class),
    PLAYER_TOTAL_ONLINE(9, PlayerData.class),
    PLAYER_JOINED(10, PlayerData.class),
    PLAYER_EXTRA_PERMS(11, PlayerData.class, Validation.PERMISSION),
    PLAYER_COINS(12, PlayerData.class),
    PLAYER_BANPOINTS(13, PlayerData.class, Validation.INTEGER);

    /**
     * The class who inherits the fields represented by the enum
     */
    @Getter
    private Class<?> wrappedClass;

    /**
     * The id of the field
     */
    private int fieldId;

    /**
     * Ids of the {@link Validation}'s for the value inside the field
     */
    @Getter
    private List<Integer> validationIds = new ArrayList<>(Collections.singletonList(Validation.SIMPLE_STRING.ordinal()));

    DbModifier(int fieldId, Class<?> wrappedClass, Validation... validations) {
        this.fieldId = fieldId;
        this.wrappedClass = wrappedClass;

        for(Validation v : validations) {
            validationIds.add(v.ordinal());
        }
    }

    /**
     * Gets the name of the field hidden behind this modifier field
     *
     * @return The name
     */
    public String getFieldName() {
        Field field = ReflectionUtil.getFieldFromId(getId(), getWrappedClass());
        if(field == null) return "";
        return field.getName();
    }

    /**
     * Gets a db modifier from field name and wrapped class
     *
     * @param key          The key
     * @param wrappedClass The class of the field
     * @return The DbModifier object
     */
    public static DbModifier fromKey(String key, Class<?> wrappedClass) {
        int fieldId = ReflectionUtil.getFieldId(ReflectionUtil.getField(key, wrappedClass), wrappedClass);

        for(DbModifier modifier : DbModifier.values()) {
            if(modifier.fieldId == fieldId && modifier.getWrappedClass().equals(wrappedClass)) {
                return modifier;
            }
        }
        return null;
    }

    /**
     * Gets the key resolved from the id and the class
     *
     * @return The key as string
     */
    public String getKey() {
        return DataResolver.getKey(getId(), getWrappedClass());
    }

    /**
     * Gets the id of the field
     *
     * @return The id as int
     */
    public int getId() {
        return fieldId;
    }

    @Override
    public String toString() {
        return getId() + "";
    }
}
