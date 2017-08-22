package de.superioz.moo.api.database.objects;

import de.superioz.moo.api.util.SimpleSerializable;
import de.superioz.moo.api.utils.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.common.punishment.BanSubType;
import de.superioz.moo.api.common.punishment.Punishmental;
import de.superioz.moo.api.database.object.DbKey;

import java.text.MessageFormat;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class Ban extends SimpleSerializable {

    /**
     * The uuid of the banned user
     */
    @DbKey
    public UUID banned;

    /**
     * The uuid of the player who banned the player (null if console)
     */
    @DbKey
    public UUID by;

    /**
     * The timestamp of the ban execution
     */
    @DbKey
    public Long start;

    /**
     * How long is the player banned?
     */
    @DbKey
    public Long duration;

    /**
     * Id of the {@link BanSubType}
     */
    @DbKey
    public Integer subTypeId;

    /**
     * The reason of the ban (either a subReason of the {@link #subType} or a custom one)<br>
     * This reason will not be shown to the user.
     */
    @DbKey
    public String reason;

    /**
     * The banPoints which are then assigned to the player
     * (Will be calculated just before the ban)
     */
    @DbKey
    public Integer banPoints;

    private BanSubType subType;

    public Ban(UUID by, BanSubType subType, String reason) {
        this.by = by;
        this.subTypeId = subType.getId();
        this.subType = subType;
        this.reason = reason;
        this.subType = subType;
    }

    public Ban(UUID by, BanSubType subType, String reason, long duration) {
        this(by, subType, reason);
        this.duration = duration;
    }

    /**
     * Applies the message and replaces variables
     *
     * @param s The string
     * @return The applied string
     */
    public String apply(String s) {
        if(isPermanent()) {
            return MessageFormat.format(s, getSubType().getName());
        }
        else {
            return MessageFormat.format(s, TimeUtil.getFormat(start + duration), getSubType().getName());
        }
    }

    /**
     * Gets the time when the ban runs out
     *
     * @return The time as ms
     */
    public long until() {
        return start + duration;
    }

    /**
     * Checks if the ban is permanent
     *
     * @return The result
     */
    public boolean isPermanent() {
        return duration == -1;
    }

    /**
     * Get the real ban reason object
     *
     * @return The object
     */
    public BanSubType getSubType() {
        if(subType != null) return subType;
        return (subType = Punishmental.getInstance().getSubType(subTypeId));
    }

}
