package de.superioz.moo.spigot.common;

import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.spigot.util.ProtocolUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.lang.reflect.Field;

/**
 * Created on 12.11.2016.
 */
public class PermissionInjector {

    /**
     * Inject new permissible base into given commandSender
     *
     * @param sender      The sender
     * @param permissible The permissible base
     * @return The result
     */
    public static boolean inject(CommandSender sender, Permissible permissible) {
        Field f = getPermissibleField(sender);

        try {
            f.set(sender, permissible);
            return true;
        }
        catch(IllegalAccessException e) {
            return false;
        }
    }

    /**
     * Get permissible object from given sender
     *
     * @param sender The sender
     * @return The permissible object
     */
    public static Permissible getPermissible(CommandSender sender) {
        Field f = getPermissibleField(sender);

        if(f == null) {
            return null;
        }
        try {
            return (Permissible) f.get(sender);
        }
        catch(IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the permissible object from sender
     *
     * @param sender The sender
     * @return The result
     */
    public static CustomPermissible getCustomPermissible(CommandSender sender) {
        Field f = getPermissibleField(sender);

        if(f == null) return null;
        try {
            return (CustomPermissible) f.get(sender);
        }
        catch(IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Get the permissible field
     *
     * @param sender The command sender (either player or console)
     * @return The field
     */
    public static Field getPermissibleField(CommandSender sender) {
        Field f = null;

        if(sender instanceof Player) {
            f = ReflectionUtil.getField("perm", ProtocolUtil.getOBCClassExact("entity.CraftHumanEntity"));
        }
        else if(sender instanceof ConsoleCommandSender) {
            f = ReflectionUtil.getField("perm", ProtocolUtil.getOBCClassExact("command.ServerCommandSender"));
        }
        if(f != null) f.setAccessible(true);
        return f;
    }

}
