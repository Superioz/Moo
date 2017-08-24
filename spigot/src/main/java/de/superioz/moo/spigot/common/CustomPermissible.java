package de.superioz.moo.spigot.common;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.common.GroupPermission;
import de.superioz.moo.client.util.PermissionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.*;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * This class will be injected into the player class to handle
 * permission events
 */
public class CustomPermissible extends PermissibleBase {

    @Getter
    private UUID uniqueId;
    @Getter
    private Permissible oldPermissible;
    @Getter
    private CommandSender sender;

    public CustomPermissible(CommandSender sender, UUID uuid, Permissible oldPermissible) {
        super(sender);
        this.sender = sender;
        this.uniqueId = uuid;
        this.oldPermissible = oldPermissible;
    }

    public List<GroupPermission> getPermissions() {
        List<String> l = MooCache.getInstance().getPlayerPermissionMap().get(uniqueId);
        List<GroupPermission> permissions = new ArrayList<>();

        for(String s : l) {
            permissions.add(new GroupPermission(s));
        }
        return permissions;
    }

    @Override
    public boolean isOp() {
        return super.isOp();
    }

    @Override
    public void setOp(boolean value) {
        super.setOp(value);
    }

    @Override
    public boolean isPermissionSet(String name) {
        for(String s : MooCache.getInstance().getPlayerPermissionMap().get(uniqueId)) {
            if(s.equals("*")) return true;
            s = s.substring(2, s.length());
            if(s.startsWith("-")) {
                s = s.substring(1, s.length());
            }

            if(s.endsWith("*")) {
                s = s.substring(0, s.length() - 1);
            }
            if(name.startsWith(s)) return true;
        }
        return false;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return this.isPermissionSet(perm.getName());
    }

    @Override
    public boolean hasPermission(String inName) {
        String name = inName.toLowerCase();

        if(isPermissionSet(name)) {
            return PermissionUtil.hasPermission(inName, false, MooCache.getInstance().getPlayerPermissionMap().get(uniqueId));
        }
        else {
            Permission perm = Bukkit.getServer().getPluginManager().getPermission(name);

            if(perm != null) {
                return perm.getDefault().getValue(isOp());
            }
            else {
                return Permission.DEFAULT_PERMISSION.getValue(isOp());
            }
        }
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return this.hasPermission(perm.getName());
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return super.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return super.addAttachment(plugin);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        super.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        //
    }

    @Override
    public synchronized void clearPermissions() {
        //
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return super.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return super.addAttachment(plugin, ticks);
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        Set<PermissionAttachmentInfo> permissionList = new HashSet<>();

        for(GroupPermission w : getPermissions()) {
            PermissionAttachmentInfo i = new PermissionAttachmentInfo(this, w.getPerm(), null, !w.isNegative());
            permissionList.add(i);

            Permission perm = Bukkit.getPluginManager().getPermission(i.getPermission());
            if(perm != null && !perm.getChildren().isEmpty()) {
                Map<String, Boolean> ch = perm.getChildren();

                for(String s : ch.keySet()) {
                    PermissionAttachmentInfo i0 = new PermissionAttachmentInfo(this,
                            s, null, ch.get(s));
                    permissionList.add(i0);
                }
            }
        }
        return permissionList;
    }
}
