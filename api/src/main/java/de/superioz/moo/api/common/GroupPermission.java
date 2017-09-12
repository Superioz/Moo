package de.superioz.moo.api.common;

import lombok.Getter;

import java.util.Comparator;

@Getter
public class GroupPermission {

    public static final Comparator<GroupPermission> PERMISSION_COMPARATOR = (o1, o2) -> o1.getRawPerm().compareTo(o2.getRawPerm());

    /**
     * Is the permission an all star permission? (="*")<br>
     * That would mean that this is a everything-allowed-permission
     */
    private boolean allStar;

    /**
     * Permission for the proxy server? (otherwise only for the child instance)
     */
    private boolean proxied;

    /**
     * Permission for both proxy and spigot?
     */
    private boolean star;

    /**
     * Negative permission would mean this permission gets removed from the player<br>
     * Example: The player has the permission command.*, that'd mean he could use every command,
     * but with a negative permission like -command.fly fly would be the only command he can't use
     */
    private boolean negative;

    /**
     * The permission
     */
    private String perm;

    /**
     * The raw permission
     */
    private String rawPerm;

    public GroupPermission(String perm) {
        this.rawPerm = perm;
        if(perm.equals("*")) {
            allStar = true;
            star = true;
        }
        else {
            proxied = perm.startsWith("b:");
            star = perm.startsWith("*:") || perm.equals("*");
            perm = perm.substring(2, perm.length());
            negative = perm.startsWith("-");
            if(negative) perm = perm.substring(1, perm.length());
        }
        this.perm = perm;
    }

}
