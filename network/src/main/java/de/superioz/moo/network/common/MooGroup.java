package de.superioz.moo.network.common;

import de.superioz.moo.api.common.ObjectWrapper;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.network.queries.MooQueries;

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
    public MooGroup setName(String name) {
        wrappedObject.setName(name);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_NAME, name);
        }
        return this;
    }

    /**
     * Sets the rank of the group
     *
     * @param rank The rank
     * @return This
     */
    public MooGroup setRank(int rank) {
        wrappedObject.setRank(rank);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_RANK, rank);
        }
        return this;
    }

    /**
     * Sets the permissions of this group
     *
     * @param permissions The permissions
     * @return This
     */
    public MooGroup setPermissions(List<String> permissions) {
        wrappedObject.setPermissions(permissions);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_PERMISSIONS, permissions);
        }
        return this;
    }

    /**
     * Sets the parents of this group
     *
     * @param parents The parents
     * @return This
     */
    public MooGroup setParents(List<String> parents) {
        wrappedObject.setParents(parents);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_INHERITANCES, parents);
        }
        return this;
    }

    /**
     * Sets the prefix of this group
     *
     * @param prefix The prefix
     * @return This
     */
    public MooGroup setPrefix(String prefix) {
        wrappedObject.setPrefix(prefix);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_PREFIX, prefix);
        }
        return this;
    }

    /**
     * Sets the suffix of this group
     *
     * @param suffix The suffix
     * @return This
     */
    public MooGroup setSuffix(String suffix) {
        wrappedObject.setSuffix(suffix);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_SUFFIX, suffix);
        }
        return this;
    }

    /**
     * Sets the color of this group
     *
     * @param color The color
     * @return This
     */
    public MooGroup setColor(String color) {
        wrappedObject.setColor(color);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_COLOR, color);
        }
        return this;
    }

    /**
     * Sets the tab prefix of this group
     *
     * @param tabPrefix The tab prefix
     * @return This
     */
    public MooGroup setTabPrefix(String tabPrefix) {
        wrappedObject.setTabPrefix(tabPrefix);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_TAB_PREFIX, tabPrefix);
        }
        return this;
    }

    /**
     * Sets the tab suffix of this group
     *
     * @param tabSuffix The tab suffix
     * @return This
     */
    public MooGroup setTabSuffix(String tabSuffix) {
        wrappedObject.setTabSuffix(tabSuffix);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyGroup(getName(), DbModifier.GROUP_TAB_SUFFIX, tabSuffix);
        }
        return this;
    }

}
