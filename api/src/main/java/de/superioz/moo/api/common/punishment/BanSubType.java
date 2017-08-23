package de.superioz.moo.api.common.punishment;

import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.TimeUtil;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BanSubType {

    public static final Pattern REGEX = Pattern.compile("[a-z\\-]*:(" + Validation.TIME.getRawRegex() + ")(;[a-z]*)?");

    private String rawString;

    private String name;
    private int duration;
    private TimeUnit timeUnit;
    private BanType banType;

    private int id;

    public BanSubType(String s, int id) {
        if(!REGEX.matcher(s).matches()) return;
        this.rawString = s;
        this.id = id;

        String[] split = s.split(":");
        this.name = split[0];
        String[] split0 = split[1].split(";");

        // list duration
        Pair<Integer, TimeUnit> timeDuration = TimeUtil.getTime(split0[0]);
        this.duration = timeDuration.getKey();
        this.timeUnit = timeDuration.getValue();

        // banType
        this.banType = split0.length > 1
                ? split0[1].equalsIgnoreCase(BanType.CHAT.name())
                ? BanType.CHAT : BanType.GLOBAL : BanType.GLOBAL;
    }

}
