package de.superioz.moo.api.config;

import com.mongodb.client.model.Filters;
import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.utils.ReflectionUtil;
import org.bson.Document;

public final class MooConfig {

    private DatabaseConnection connection;

    public MooConfig(DatabaseConnection connection) {
        this.connection = connection;
    }

    /**
     * Sets a value to the given config type
     *
     * @param type The config type
     * @param val  The value to be set
     */
    public void set(MooConfigType type, String val) {
        Object castedVal = ReflectionUtil.safeCast(val);
        connection.upsert(DatabaseType.CONFIG, Filters.eq(DbModifier.CONFIG_CATEGORY.getFieldName(), type.getCategory().getName()),
                new DbQuery().equate(type.getKey(), castedVal).toDocument(), aLong -> {
                });

        // set to moo cache
        MooCache.getInstance().getConfigMap().putAsync(type.getKey(), castedVal);
    }

    /**
     * Loads the config from the database
     */
    public void load() {
        for(MooConfigCategory category : MooConfigCategory.values()) {
            String databaseKey = category.getName();

            // fine the document with given key
            connection.findOne(DatabaseType.CONFIG, Filters.eq(DbModifier.CONFIG_CATEGORY.getFieldName(), databaseKey), document -> {
                if(document == null) {
                    // create the document with default values
                    Document defaultDocument = new Document();

                    for(MooConfigType configType : MooConfigType.values()) {
                        defaultDocument.put(configType.getKey(), configType.getDefaultValue());
                    }

                    connection.insert(DatabaseType.CONFIG, defaultDocument);
                    load(defaultDocument);
                    return;
                }

                // otherwise get all the values
                load(document);
            });
        }
    }

    private void load(Document document) {
        for(MooConfigType configType : MooConfigType.values()) {
            Object val = document.get(configType.getKey());

            if(val == null) return;
            MooCache.getInstance().getConfigMap().fastPutAsync(configType.getKey(), val);
        }
    }


}
