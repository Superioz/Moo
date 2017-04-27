package de.superioz.moo.spigot.cache;

import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.client.util.PermissionUtil;
import de.superioz.moo.protocol.packets.PacketConfig;
import de.superioz.moo.spigot.Lightning;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created on 21.10.2016.
 */
public class ProxyCache {

    public static final HashMap<String, Group> GROUP_CACHE = new HashMap<>();
    public static final HashMap<UUID, PlayerData> PLAYERDATA_CACHE = new HashMap<>();
    public static final HashMap<UUID, List<String>> PERMISSION_CACHE = new HashMap<>();

    public static final HashMap<PacketConfig.Type, String> CONFIG = new HashMap<>();

    /*public static final LoadingCache<String, UUID> UUID_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES).build(new CacheLoader<String, UUID>() {
                @Override
                public UUID load(String s) throws Exception{
                    PacketRespond respond = Moo.getInstance().getPlayerManager().getBuf(s);

                    if(respond.status == ResponseStatus.OK){
                        UniqueIdBuf buf = UniqueIdBuf.fromString(respond.message);

                        return buf == null ? null : buf.uuid;
                    }
                    return null;
                }
            });*/

    /**
     * Get the group of the given player
     *
     * @param uuid The uniqueId
     * @return The group
     */
    public static Group getGroup(UUID uuid) {
        return getGroup(getPlayerData(uuid).group);
    }

    /**
     * Get the player data from given uniqueId
     *
     * @param uuid The uniqueId
     * @return The playerData object
     */
    public static PlayerData getPlayerData(UUID uuid) {
        return PLAYERDATA_CACHE.get(uuid);
    }

    /**
     * Get permissions of given user
     *
     * @param uuid The uniqueId
     * @return The list of permissions
     */
    public static List<String> getPermissions(UUID uuid) {
        return PERMISSION_CACHE.get(uuid);
    }

    /**
     * Get all groups
     *
     * @return The groups as list
     */
    public static List<Group> getGroups() {
        return new ArrayList<>(GROUP_CACHE.values());
    }

    /**
     * Checks if the cache contains group
     *
     * @param name The name
     * @return The result
     */
    public static boolean hasGroup(String name) {
        return GROUP_CACHE.containsKey(name);
    }

    /**
     * Returns the group by name
     *
     * @param name The groupName
     * @return The group object
     */
    public static Group getGroup(String name) {
        return GROUP_CACHE.get(name);
    }

    /**
     * Updates the cache with given values
     *
     * @param type The type (database)
     * @param data The data (meta-data from packets)
     * @param mod  The modify type (0-3)
     * @return The result
     */
    public static boolean update(String type, String data, int mod) {
        if(type.equalsIgnoreCase(DatabaseType.GROUP.name())) {
            if(mod == 1) {
                GROUP_CACHE.remove(data);

                // GROUP HAS BEEN DELETED
                for(Player player : Bukkit.getOnlinePlayers()) {
                    updatePermission(player.getUniqueId());
                }
            }
            else if(mod == 0 || mod == 2) {
                Group group = ReflectionUtil.deserialize(data, Group.class);
                GROUP_CACHE.put(group.name, group);

                // GROUP HAS BEEN UPDATED
                if(mod == 2) {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        updatePermission(player.getUniqueId());
                    }
                }
            }
            else if(mod == 3) {
                List<String> l = StringUtil.split(data);
                Object id = ReflectionUtil.safeCast(l.get(0));
                Group group = ReflectionUtil.deserialize(l.get(1), Group.class);

                GROUP_CACHE.remove((String) id);
                GROUP_CACHE.put(group.name, group);

                for(Player player : Bukkit.getOnlinePlayers()) {
                    updatePermission(player.getUniqueId());
                }
            }
        }
        else if(type.equalsIgnoreCase(DatabaseType.PLAYER.name())) {
            if(mod == 1) {
                PLAYERDATA_CACHE.remove(UUID.fromString(data));

                // PLAYERDATA HAS BEEN DELETED ? WHAT THE FLACK?
            }
            else if(mod == 0 || mod == 2) {
                PlayerData playerData = ReflectionUtil.deserialize(data, PlayerData.class);
                PLAYERDATA_CACHE.put(playerData.uuid, playerData);

                // UPDATE PERMISSIONS IF EDITED
                if(mod == 2) {
                    updatePermission(playerData.uuid);
                }
            }
            else if(mod == 3) {
                // WTF?
            }
        }
        return true;
    }

    /**
     * Updates permissions for given uniqueId
     *
     * @param uuid The uniqueId
     * @return The result
     */
    public static boolean updatePermission(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(Lightning.getInstance(), () -> {
            if(!PLAYERDATA_CACHE.containsKey(uuid)) return;
            PlayerData data = PLAYERDATA_CACHE.get(uuid);
            String groupName = data.group;

            // ..
            if(!GROUP_CACHE.containsKey(groupName)) {
                Group group = new Group();
                group.name = groupName;


                GROUP_CACHE.put(groupName, group);
            }
            Group group = GROUP_CACHE.get(groupName);

            List<String> permissions = new ArrayList<>(data.extraPerms);
            permissions.addAll(PermissionUtil.getAllPermissions(group, new ArrayList<>(GROUP_CACHE.values())));
            PERMISSION_CACHE.put(data.uuid, permissions);
        });
        return true;
    }

    /**
     * Applies given data into caches
     *
     * @param data  The playerData
     * @param group The group from the player
     * @return The result
     */
    public static boolean apply(PlayerData data, Group group) {
        if(data == null || group == null) return false;
        PLAYERDATA_CACHE.put(data.uuid, data);

        updatePermission(data.uuid);
        return true;
    }

    /**
     * Removes data from caches
     *
     * @param data The data
     * @return Result
     */
    public static boolean remove(PlayerData data) {
        PLAYERDATA_CACHE.remove(data.uuid);
        PERMISSION_CACHE.remove(data.uuid);
        return true;
    }

    // Loads all groups from cload
    /*public static boolean loadGroups() {
        Bukkit.getScheduler().runTaskAsynchronously(Lightning.getInstance(), () -> {
            PacketRespond respond = null;
            try {
                respond = Moo.getInstance().getGroupManager().list();
            }
            catch(MooOutputException e) {
                return;
            }
            if(respond.status != ResponseStatus.OK) return;
            List<String> groupsAsStringList = Stringerino.split(respond.getMessage());

            //
            List<Group> groupList = new ArrayList<>();
            groupsAsStringList.forEach(s -> {
                Group g = Group.fromString(s);
                if(g != null) groupList.add(g);
            });
            groupList.forEach(group -> GROUP_CACHE.put(group.name, group));
        });
        return true;
    }*/

}
