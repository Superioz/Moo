package de.superioz.moo.cloud.database;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.cloud.cache.GroupCache;

import java.util.List;

public class GroupCollection extends DatabaseCollection<String, Group> {

    public GroupCollection(DatabaseConnection backend) {
        super(backend);
        super.architecture(Group.class);
        super.cache(new GroupCache(new DatabaseCache.Builder().maximumSize(100).database(this)));
    }

    @Override
    public String getName() {
        return DatabaseType.GROUP.getName();
    }

    /**
     * Gets the default group
     *
     * @return The group
     */
    public Group getDefault() {
        List<Group> groups = getCache().asList();

        Group def = null;
        for(Group g : groups) {
            if(g.isDefault()) {
                def = g;
                break;
            }
        }
        if(def != null) return def;
        else {
            // get the default group
            def = get("default");
            if(def == null) {
                def = new Group();
                def.name = "default";
            }
            def.rank = 0;

            // create or update the group
            set(def.name, def, true);
        }
        return def;
    }

}
