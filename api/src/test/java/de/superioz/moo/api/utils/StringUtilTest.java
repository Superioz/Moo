package de.superioz.moo.api.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class StringUtilTest {

    @Test
    void testFormatting(){
        Map<String, String> entries = new HashMap<>();
        entries.put("click-to-execute", "&7Click to execute &f{0}");

        System.out.println(StringUtil.REPLACE_REGEX);
        String message = "&8# &r${\"&7{0}\",\"{click-to-execute}(/group info {0})\",2\"/group info {0}\"}$ &7(Rank: &c{2}&7)";
        String str = StringUtil.format(message, entries::get, "default", 3);
        System.out.println(" ");
        System.out.println(str);
    }

}
