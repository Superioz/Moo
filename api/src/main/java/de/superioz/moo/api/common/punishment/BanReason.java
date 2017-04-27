package de.superioz.moo.api.common.punishment;

import de.superioz.moo.api.collection.MultiMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BanReason {

    public static final Pattern REGEX = Pattern.compile("[a-z\\-]*(:[a-z\\-]*)");

    private String name;
    private String banSubTypeStr;
    private BanSubType banSubType;

    public BanReason(String s) {
        if(!REGEX.matcher(s).matches()) return;

        String[] split = s.split(":");
        this.name = split[0];
        this.banSubTypeStr = split[1];
    }

    /**
     * Initialises the ban reason by checking for the ban sub type
     * inside the map and putting this inside there
     *
     * @param banTypes The bantypes map
     */
    public void init(MultiMap<BanSubType, BanReason> banTypes) {
        if(banSubTypeStr == null) return;
        for(BanSubType type : banTypes.keySet()) {
            if(banSubTypeStr.equalsIgnoreCase(type.getName())) {
                this.banSubType = type;
                banTypes.add(type, this);
                break;
            }
        }
    }

    /**
     * Gets the ban type of the reason
     *
     * @return The type object
     */
    public BanType getType() {
        return getBanSubType().getBanType();
    }

}
