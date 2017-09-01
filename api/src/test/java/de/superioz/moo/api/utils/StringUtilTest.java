package de.superioz.moo.api.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class StringUtilTest {

    @Test
    void testFormatting(){
        Map<String, String> entries = new HashMap<>();
        entries.put("click-to-execute", "&7Click to execute &f{0}");

        String message = "&7Permissions: ${\"&c{0}\",\"{click-to-execute}(/perm list -g {1})\",2\"/perm list -g {1}\"}$";

        String str = StringUtil.format(message, entries::get, 3, "default");
        System.out.println(" ");
        System.out.println(str);
    }

}
