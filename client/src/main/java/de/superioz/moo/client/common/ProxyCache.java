package de.superioz.moo.client.common;

import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.client.events.PermissionUpdateEvent;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketRespond;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * This class is for caching player data and similar<br>
 * That includes: {@link PlayerData}s, permissions and {@link Group}s
 *
 * TODO remove this class and put everything into MooCache
 */
public final class ProxyCache implements PacketAdapter {

    private static ProxyCache instance;

    // singleton
    public static synchronized ProxyCache getInstance() {
        if(instance == null) {
            instance = new ProxyCache();
        }
        return instance;
    }

    private HashMap<String, Group> groupMap = new HashMap<>();
    private HashMap<UUID, PlayerData> uuidPlayerdataMap = new HashMap<>();
    private HashMap<UUID, List<String>> uuidPermissionMap = new HashMap<>();

    @PacketHandler
    public void onRespond(PacketRespond respond) {
        if(respond == null || respond.status != ResponseStatus.OK) return;
        String header = respond.header;
        if(!header.startsWith("mod-")) return;

        // ..
        String type = header.replaceFirst("mod-", "");
        for(String s : respond.message) {

            String[] s0 = s.split(":", 2);
            int mod = Integer.parseInt(s0[0]);
            ProxyCache.getInstance().update(type, s0[1], mod);
        }
    }

    /**
     * Get the player data from given uniqueId
     *
     * @param uuid The uniqueId
     * @return The playerData object
     */
    public PlayerData getPlayerData(UUID uuid) {
        return uuidPlayerdataMap.get(uuid);
    }

    /**
     * Get permissions of given user
     *
     * @param uuid The uniqueId
     * @return The list of permissions
     */
    public List<String> getPermissions(UUID uuid) {
        if(!uuidPermissionMap.containsKey(uuid)) return new ArrayList<>();
        return uuidPermissionMap.get(uuid);
    }

    /**
     * Get all groups
     *
     * @return The groups as list
     */
    public List<Group> getGroups() {
        return new ArrayList<>(groupMap.values());
    }

    /**
     * Returns the group by name
     *
     * @param name The groupName
     * @return The group object
     */
    public Group getGroup(String name) {
        return groupMap.get(name);
    }

    /**
     * Updates the cache with given values
     *
     * @param type The type (database)
     * @param data The data (meta-data from packets)
     * @param mod  The modify type (0-3)(0 = create; 1 = delete; 2 = modify; 3 = primkey)
     * @return The result
     */
    public boolean update(String type, String data, int mod) {
        if(type.equalsIgnoreCase(DatabaseType.GROUP.name())) {
            if(mod == 1) {
                groupMap.remove(data);

                // GROUP HAS BEEN DELETED
                EventExecutor.getInstance().execute(new PermissionUpdateEvent());
            }
            else if(mod == 0 || mod == 2) {
                Group group = ReflectionUtil.deserialize(data, Group.class);
                groupMap.put(group.name, group);

                // GROUP HAS BEEN UPDATED
                if(mod == 2) {
                    EventExecutor.getInstance().execute(new PermissionUpdateEvent());
                }
            }
            else if(mod == 3) {
                List<String> l = StringUtil.split(data);
                Object id = ReflectionUtil.safeCast(l.get(0));
                Group group = ReflectionUtil.deserialize(l.get(1), Group.class);

                groupMap.remove((String) id);
                groupMap.put(group.name, group);

                EventExecutor.getInstance().execute(new PermissionUpdateEvent());
            }
        }
        else if(type.equalsIgnoreCase(DatabaseType.PLAYER.name())) {
            if(mod == 1) {
                uuidPlayerdataMap.remove(UUID.fromString(data));

                // PLAYERDATA HAS BEEN DELETED ? WHAT THE FLACK?
            }
            else if(mod == 0 || mod == 2) {
                PlayerData playerData = ReflectionUtil.deserialize(data, PlayerData.class);
                uuidPlayerdataMap.put(playerData.uuid, playerData);

                // UPDATE PERMISSIONS IF EDITED
                if(mod == 2) {
                    MooQueries.getInstance().updatePermission(playerData.uuid);
                }
            }
            else if(mod == 3) {
                // WTF?
            }
        }
        return true;
    }

    /**
     * Applies given data into caches
     *
     * @param data  The playerData
     * @param group The group from the player
     * @return The result
     */
    public boolean apply(PlayerData data, Group group) {
        if(data == null || group == null) return false;
        uuidPlayerdataMap.put(data.uuid, data);

        MooQueries.getInstance().updatePermission(data.uuid);
        return true;
    }

    /**
     * Removes data from caches
     *
     * @param data The data
     * @return Result
     */
    public boolean remove(PlayerData data) {
        uuidPlayerdataMap.remove(data.uuid);
        uuidPermissionMap.remove(data.uuid);
        return true;
    }

}
