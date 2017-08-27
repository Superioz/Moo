package de.superioz.moo.cloud.database;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.cloud.database.cache.GroupCache;

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
            // list the default group
            def = get("default");
            if(def == null) {
                def = new Group();
                def.setName("default");
            }
            def.setRank(0);

            // create or update the group
            set(def.getName(), def, true);
        }
        return def;
    }

}
