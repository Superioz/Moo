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
    private String banReasonStr;
    private BanCategory banCategory;

    public BanReason(String s) {
        if(!REGEX.matcher(s).matches()) return;

        String[] split = s.split(":");
        this.name = split[0];
        this.banReasonStr = split[1];
    }

    /**
     * Initialises the ban reason by checking for the ban sub type
     * inside the map and putting this inside there
     *
     * @param banReasons The bantypes map
     */
    public void init(MultiMap<BanCategory, BanReason> banReasons) {
        if(banReasonStr == null) return;
        for(BanCategory category : banReasons.keySet()) {
            if(banReasonStr.equalsIgnoreCase(category.getName())) {
                this.banCategory = category;
                banReasons.add(category, this);
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
        return getBanCategory().getBanType();
    }

}
