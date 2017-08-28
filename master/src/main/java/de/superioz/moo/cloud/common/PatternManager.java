package de.superioz.moo.cloud.common;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.netty.client.ClientType;
import de.superioz.moo.netty.common.PacketMessenger;
import de.superioz.moo.netty.packets.PacketPatternState;

public final class PatternManager {

    private static PatternManager instance;

    public static PatternManager getInstance() {
        if(instance == null) instance = new PatternManager();
        return instance;
    }

    /**
     * Creates given pattern by putting it into the database & cache
     *
     * @param pattern The pattern
     * @return The result
     */
    public boolean createPattern(ServerPattern pattern) {
        // add to database
        if(!DatabaseCollections.PATTERN.set(pattern.getName(), pattern)) return false;

        // put in cache
        MooCache.getInstance().getPatternMap().putAsync(pattern.getName(), pattern);

        return true;
    }

    /**
     * Deletes pattern by removing it from cache & database
     *
     * @param pattern The pattern
     * @return The result
     */
    public boolean deletePattern(ServerPattern pattern) {
        // remove from database
        if(!DatabaseCollections.PATTERN.delete(pattern.getName(), true)) return false;

        // remove from cache
        MooCache.getInstance().getPatternMap().removeAsync(pattern.getName());

        // remove folder
        PacketMessenger.message(new PacketPatternState(pattern.getName(), false), ClientType.DAEMON);

        return true;
    }

}
