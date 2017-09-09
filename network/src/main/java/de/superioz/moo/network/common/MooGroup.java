package de.superioz.moo.network.common;

import de.superioz.moo.api.common.ObjectWrapper;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.redis.MooCache;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.network.queries.MooQueries;
import de.superioz.moo.network.queries.ResponseStatus;

import java.util.List;

/**
 * Wrapper class for {@link Group}
 */
public class MooGroup extends ObjectWrapper<MooGroup, Group> {

    public MooGroup(Group wrappedObject) {
        super(wrappedObject);
    }

    @Override
    public void update() {
        this.wrappedObject = MooCache.getInstance().getGroupMap().get(getName());
    }

    /**
     * Gets the name of this group
     *
     * @return The name
     */
    public String getName() {
        return wrappedObject.getName();
    }

    /**
     * Gets the rank of this group
     *
     * @return The rank
     */
    public int getRank() {
        return wrappedObject.getRank();
    }

    /**
     * Gets the raw permissions of this group
     *
     * @return The permissions
     */
    public List<String> getPermissions() {
        return wrappedObject.getPermissions();
    }

    /**
     * Gets the parents of this group (other group's names)
     *
     * @return The parents
     */
    public List<String> getParents() {
        return wrappedObject.getParents();
    }

    /**
     * Gets the prefix of this group (chat)
     *
     * @return The prefix
     */
    public String getPrefix() {
        return wrappedObject.getPrefix();
    }

    /**
     * Gets the suffix of this group (chat)
     *
     * @return The suffix
     */
    public String getSuffix() {
        return wrappedObject.getSuffix();
    }

    /**
     * Gets the color of this group (chat, ...)
     *
     * @return The color
     */
    public String getColor() {
        return wrappedObject.getColor();
    }

    /**
     * Gets the tab prefix of this group
     *
     * @return The tab prefix
     */
    public String getTabPrefix() {
        return wrappedObject.getTabPrefix();
    }

    /**
     * Gets the tab suffix of this group
     *
     * @return The tab suffix
     */
    public String getTabSuffix() {
        return wrappedObject.getTabSuffix();
    }

    /*
    ===================
    SETTER
    ===================
     */

    /**
     * Sets the name of the group
     *
     * @param name The name
     * @return This
     */
    public ResponseStatus setName(String name) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setName(name);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_NAME, name);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the rank of the group
     *
     * @param rank The rank
     * @return This
     */
    public ResponseStatus setRank(int rank) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setRank(rank);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_RANK, rank);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the permissions of this group
     *
     * @param permissions The permissions
     * @return This
     */
    public ResponseStatus setPermissions(List<String> permissions) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setPermissions(permissions);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_PERMISSIONS, permissions);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the parents of this group
     *
     * @param parents The parents
     * @return This
     */
    public ResponseStatus setParents(List<String> parents) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setParents(parents);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_INHERITANCES, parents);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the prefix of this group
     *
     * @param prefix The prefix
     * @return This
     */
    public ResponseStatus setPrefix(String prefix) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setPrefix(prefix);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_PREFIX, prefix);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the suffix of this group
     *
     * @param suffix The suffix
     * @return This
     */
    public ResponseStatus setSuffix(String suffix) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setSuffix(suffix);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_SUFFIX, suffix);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the color of this group
     *
     * @param color The color
     * @return This
     */
    public ResponseStatus setColor(String color) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setColor(color);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_COLOR, color);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the tab prefix of this group
     *
     * @param tabPrefix The tab prefix
     * @return This
     */
    public ResponseStatus setTabPrefix(String tabPrefix) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setTabPrefix(tabPrefix);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_TAB_PREFIX, tabPrefix);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the tab suffix of this group
     *
     * @param tabSuffix The tab suffix
     * @return This
     */
    public ResponseStatus setTabSuffix(String tabSuffix) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setTabSuffix(tabSuffix);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_TAB_SUFFIX, tabSuffix);
        }
        return ResponseStatus.OK;
    }

    /*
    ===================
    OTHERS
    ===================
     */

    /**
     * Checks if the player exists
     *
     * @return The result
     */
    public boolean exists() {
        return wrappedObject != null && wrappedObject.getName() != null;
    }

    /**
     * Adds given permissions to the player
     *
     * @param permissions The permissions
     * @return The status
     */
    public ResponseStatus addPermission(String... permissions) {
        List<String> currentPermissions = getPermissions();
        int size = currentPermissions.size();
        for(String s : permissions) {
            if(Validation.PERMISSION.matches(s)) {
                currentPermissions.add(s);
            }
        }
        if(currentPermissions.size() == size) return ResponseStatus.BAD_REQUEST;
        return setPermissions(currentPermissions);
    }

    public ResponseStatus addPermission(List<String> permissions) {
        return addPermission(permissions.toArray(new String[]{}));
    }

    /**
     * Removes given permissions from the player
     *
     * @param permissions The permissions
     * @return The status
     */
    public ResponseStatus removePermission(String... permissions) {
        List<String> currentPermissions = getPermissions();
        int size = currentPermissions.size();
        for(String s : permissions) {
            if(Validation.PERMISSION.matches(s)) {
                currentPermissions.remove(s);
            }
        }
        if(currentPermissions.size() == size) return ResponseStatus.BAD_REQUEST;
        return setPermissions(currentPermissions);
    }

    public ResponseStatus removePermission(List<String> permissions) {
        return removePermission(permissions.toArray(new String[]{}));
    }


}
