package de.superioz.moo.network.redis;

import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.StringUtil;

public class RedisTest2 {

    /*public static void main(String[] args){
        Group group = new Group();
        group.setName("admin");
        group.setRank(1);

        MooGroup mooGroup = new MooGroup(group);

        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");

        RedissonClient client = Redisson.create(config);
        RMap<String, MooGroup> map = client.getMap("myMap");
        map.put("group", mooGroup);

        System.out.println(map.get("group").getRank());
    }*/

    public static void main(String[] args){
        String s = "&7Permission syntax: ${\"&8([&es&8|&bb&8|&f*&8]&7:&8[(&c-&8)&cx.xx.xxx&8|&f*&8])|&f*\",\"&7Examples:\\n\\\n" +
                "   &eSpigot&7: &es&7:perm.ission\\n\\\n" +
                "   &bBungee&7: &bb&7:perm.ission\\n\\\n" +
                "   &fWildcard&7: &f*&7:perm.ission | &f*&7:&f* &7| &f*\",\"\"}$ &7(&f* &7:= wildcard)";
        for(String s2 : StringUtil.split(s, Validation.MESSAGE_COMP_EVENT.getRawRegex(), true)) {
            if(Validation.MESSAGE_COMP_EVENT.matches(s2)) {
                System.out.println("EVENT: " + s2);
            }
            else {
                System.out.println("NOT: " + s2);
            }
        }
    }

}
