package de.superioz.moo.api.config;

import com.mongodb.client.model.Filters;
import de.superioz.moo.api.common.punishment.PunishmentManager;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.redis.MooCache;
import de.superioz.moo.api.utils.ReflectionUtil;
import org.bson.Document;

public final class NetworkConfig {

    private DatabaseConnection connection;

    public NetworkConfig(DatabaseConnection connection) {
        this.connection = connection;
    }

    /**
     * Gets a config value out of the cache
     *
     * @param key The key
     * @param <E> The elementtype
     * @return The value as E
     */
    public <E> E get(String key) {
        return (E) MooCache.getInstance().getConfigMap().get(key);
    }

    /**
     * Sets a value to the given config type
     *
     * @param type The config type
     * @param val  The value to be set
     */
    public void set(NetworkConfigType type, String val) {
        Object castedVal = ReflectionUtil.safeCast(val);
        connection.upsert(DatabaseType.CLOUD_CONFIG, Filters.eq(DbModifier.CONFIG_CATEGORY.getFieldName(), type.getCategory().getName()),
                new DbQuery().equate(type.getKey(), castedVal).toDocument(), aLong -> {
                });

        // set to moo cache
        MooCache.getInstance().getConfigMap().putAsync(type.getKey(), castedVal);
    }

    /**
     * Loads the config from the database
     */
    public void load() {
        for(NetworkConfigCategory category : NetworkConfigCategory.values()) {
            if(category == NetworkConfigCategory.NONE) {
                load(category, null);
                continue;
            }
            String databaseKey = category.getName();

            // fine the document with given key
            connection.findOne(DatabaseType.CLOUD_CONFIG, Filters.eq(DbModifier.CONFIG_CATEGORY.getFieldName(), databaseKey), document -> {
                if(document == null) {
                    // create the document with default values
                    Document defaultDocument = new Document();

                    defaultDocument.put(DbModifier.CONFIG_CATEGORY.getFieldName(), databaseKey);
                    for(NetworkConfigType configType : NetworkConfigType.getConfigTypes(category)) {
                        defaultDocument.put(configType.getKey(), configType.getDefaultValue());
                    }

                    connection.insert(DatabaseType.CLOUD_CONFIG, defaultDocument);
                    load(category, defaultDocument);
                    return;
                }

                // otherwise get all the values
                load(category, document);
            });
        }

        // init PunishmentManager
        PunishmentManager.getInstance().init(
                get(NetworkConfigType.PUNISHMENT_CATEGORIES.getKey()),
                get(NetworkConfigType.PUNISHMENT_SUBTYPES.getKey())
        );
    }

    private void load(NetworkConfigCategory category, Document document) {
        for(NetworkConfigType configType : NetworkConfigType.getConfigTypes(category)) {
            Object val = document != null ? document.get(configType.getKey()) : configType.getDefaultValue();

            if(val == null) return;
            MooCache.getInstance().getConfigMap().fastPutAsync(configType.getKey(), val);
        }
    }


}
