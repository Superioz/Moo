package de.superioz.moo.api.utils;

import de.superioz.moo.api.common.GroupPermission;
import de.superioz.moo.api.database.objects.Group;

import java.util.*;

/**
 * A helper class for group & permissions
 */
public final class PermissionUtil {

    /**
     * Gets all permissions from given base (parents from groups)
     *
     * @param base   The base
     * @param groups The list of groups to list all parents
     * @return The listOfString
     */
    public static Set<String> getAllPermissions(Group base, Collection<Group> groups) {
        Set<String> permissions = new HashSet<>(base.getPermissions());
        Set<String> inheritances = new HashSet<>();

        boolean finished = false;

        List<String> copy = new ArrayList<>(base.getParents());
        while(!finished){
            List<String> l = new ArrayList<>();

            for(Group g : groups) {
                if(copy.contains(g.getName())) {
                    l.addAll(g.getParents());
                }
            }

            if(l.isEmpty()) finished = true;
            else {
                inheritances.addAll(copy);
                copy = l;
            }
        }
        groups.forEach(group -> {
            if(inheritances.contains(group.getName())) permissions.addAll(group.getPermissions());
        });
        return permissions;
    }

    public static List<GroupPermission> getPermissions(List<String> permissions) {
        List<GroupPermission> groupPermissions = new ArrayList<>();
        permissions.forEach(s -> groupPermissions.add(new GroupPermission(s)));
        return groupPermissions;
    }

    /**
     * Checks if checked perm is inside list
     *
     * @param checkedPerm The checked perm
     * @param proxy       Is this a proxy perm
     * @param permissions The permissions
     * @return The result
     */
    public static boolean hasPermission(String checkedPerm, boolean proxy, List<String> permissions) {
        List<GroupPermission> groupPermissions = getPermissions(permissions);

        return hasGroupPermission(checkedPerm, proxy, groupPermissions);
    }

    public static boolean hasGroupPermission(String checkedPerm, boolean proxied, List<GroupPermission> groupPermissions) {
        List<String> permissions = new ArrayList<>();
        for(GroupPermission groupPermission : groupPermissions) {
            if(groupPermission.isProxied() == proxied || groupPermission.isStar()) {
                boolean b = groupPermission.isNegative();
                permissions.add((b ? "-" : "") + groupPermission.getPerm());
            }
        }

        if(permissions.contains("-" + checkedPerm)) return false;
        else if(permissions.contains(checkedPerm)) return true;
        else {
            for(String s : permissions) {
                boolean negative = s.startsWith("-");
                String raw = negative ? s.substring(1, s.length()) : s;
                if(raw.length() != 1 && raw.endsWith("*")) raw = raw.substring(0, raw.length() - 1);

                if(checkedPerm.startsWith(raw)) {
                    return !negative;
                }
            }
        }
        return permissions.contains("*");
    }

}
