package de.superioz.moo.api.common.punishment;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.collection.MultiMap;
import de.superioz.moo.api.io.MooConfigType;
import de.superioz.moo.api.utils.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Punishmental {

    private static Punishmental instance;

    public static synchronized Punishmental getInstance() {
        if(instance == null) {
            instance = new Punishmental();
        }
        return instance;
    }

    /**
     * the map of ban types and the child reasons from the config
     */
    private MultiMap<BanSubType, BanReason> banTypeMap = new MultiMap<>();

    /**
     * Initialises the {@link #banTypeMap} by using the given strings (Most likely are they from the cloud config)
     *
     * @param banSubTypes The string of the ban subtypes list
     * @param banReasons  The string of the ban reasons list
     */
    public void init(List<String> banSubTypes, List<String> banReasons) {
        // add bansubtypes to the map
        if(banSubTypes != null && !banSubTypes.isEmpty()) {
            banTypeMap.clear();

            // get ban sub types
            for(int i = 0; i < banSubTypes.size(); i++) {
                BanSubType subType = new BanSubType(banSubTypes.get(i), i);
                if(subType.getName() != null) banTypeMap.add(subType);
            }
        }

        // then add the reasons to the bansubtypes
        if(banReasons != null && !banReasons.isEmpty()) {

            // loop through ban reasons
            banReasons.forEach(s -> {
                BanReason reason = new BanReason(s);
                reason.init(banTypeMap);
                if(reason.getBanSubTypeStr() != null && !banTypeMap.containsValue(reason)) {
                    banTypeMap.add(reason.getBanSubType(), reason);
                }
            });
        }
    }

    /**
     * Simply initializes this class but with automatically getting the needed informations
     */
    public void init() {
        if(!MooCache.getInstance().isInitialized()) return;
        this.init(
                (List<String>)MooCache.getInstance().getConfigMap().get(MooConfigType.PUNISHMENT_SUBTYPES.getKey()),
                (List<String>)MooCache.getInstance().getConfigMap().get(MooConfigType.PUNISHMENT_REASONS.getKey())
        );
    }

    /**
     * Gets the ban subtype with given index
     *
     * @param i The index
     * @return The ban sub type
     */
    public BanSubType getSubType(int i) {
        return CollectionUtil.getEntrySafely(new ArrayList<>(banTypeMap.keySet()), i, null);
    }

    /**
     * Get the ban subtype with given name
     *
     * @param s The name of the subtype
     * @return The ban subtype
     */
    public BanSubType getSubType(String s) {
        for(BanSubType type : banTypeMap.keySet()) {
            if(s != null && type.getName().equalsIgnoreCase(s)) return type;
        }
        return null;
    }

    /**
     * Gets the ban reason from given string
     *
     * @param s The string
     * @return The ban reason object
     */
    public BanReason getBanReason(String s) {
        if(s == null) return null;

        for(Set<BanReason> reasons : banTypeMap.values()) {
            for(BanReason reason : reasons) {
                if(reason.getName().equalsIgnoreCase(s)) {
                    return reason;
                }
            }
        }
        return null;
    }

    public List<BanReason> getBanReasons() {
        List<BanReason> reasons = new ArrayList<>();
        banTypeMap.values().forEach(reasons::addAll);
        return reasons;
    }

    /*
    ===============================
    UTIL PART
    ===============================
     */

    /**
     * More than this points will result in a permanent ban
     */
    public static final int MAX_POINTS = 720;

    /**
     * More than this points is the fatal zone,
     * where every fatal ban (a ban with >=this_points) result in a permanent ban
     */
    public static final int FATAL_POINTS = 120;

    /**
     * Duration for one single ban point
     */
    public static final long POINT_DURATION = TimeUnit.HOURS.toMillis(6);

    /**
     * This constants are for defining the punishment heavyness.<br>
     * Every number above the last constant is permanent (= -1)<br>
     * These points will be converted into hours/days/weeks/months<br>
     * One point ^= 6 hours<br>
     */
    public static final int[] POINT_CONSTANTS = {1, 2, 3, 4, 12, 28, 84, FATAL_POINTS, 360, MAX_POINTS};

    /**
     * Gets the banpoints from {@link #POINT_CONSTANTS} where the hour value matches the constant
     *
     * @param val  The multiplier of the timeunit (e.g. 30)
     * @param unit The timeunit (e.g. DAYS)
     * @return The constant
     */
    public static int getBanPoints(int val, TimeUnit unit) {
        if(val == -1) return -1;
        int points = (int) (unit.toHours(val) / 6);
        if(points > MAX_POINTS) return -1;

        return (int) (unit.toHours(val) / 6);
    }

    public static int getBanPoints(BanSubType reason) {
        return getBanPoints(reason.getDuration(), reason.getTimeUnit());
    }

    /**
     * Calculates the new banpoints with old ban points and reason
     *
     * @param oldBanPoints The old ban points
     * @param reason       The ban reason
     * @return The ban points
     */
    public static int calculateBanPoints(int oldBanPoints, BanSubType reason) {
        int banPoints = getBanPoints(reason);
        int sumPoints = oldBanPoints + banPoints;
        int newPoints = oldBanPoints == 0 ? sumPoints : sumPoints + (sumPoints * (banPoints / (MAX_POINTS / 100)) / 100);

        // if the new ban is fatal and the points before as well
        if((oldBanPoints >= FATAL_POINTS && banPoints >= FATAL_POINTS)
                || newPoints > MAX_POINTS) return MAX_POINTS + 1;

        // get next ban step (always round up)
        int nextStep = 0;
        for(int d : POINT_CONSTANTS) {
            if(d > newPoints) {
                nextStep = d;
                break;
            }
        }
        return nextStep;
    }

    /**
     * Calculate the duration for given ban point
     *
     * @param banPoints The ban points
     * @return The duration in millis
     */
    public static long calculateDuration(int banPoints) {
        return banPoints > MAX_POINTS ? -1 : POINT_DURATION * banPoints;
    }

}
