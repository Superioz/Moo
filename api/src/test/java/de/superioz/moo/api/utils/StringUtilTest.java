package de.superioz.moo.api.utils;

public class StringUtilTest {

    /*@Test
    void testFormatting(){
        Map<String, String> entries = new HashMap<>();
        entries.put("click-to-execute", "&7Click to execute &f{0}");

        String message = "&7Permissions: ${\"&c{0}\",\"{click-to-execute}(/perm list -g {1})\",2\"/perm list -g {1}\"}$";

        String str = StringUtil.format(message, entries::get, 3, "default");
        System.out.println(" ");
        System.out.println(str);
    }*/

    /*public static void main(String[] args){
        Map<String, String> entries = new HashMap<>();
        entries.put("click-to-execute", "&7Click to execute &f{0}");

        String message = "&7Permissions: ${\"&c{0}\",\"{click-to-execute}(/perm list -g {1})\",2\"/perm list -g {1}\"}$";

        String str = StringUtil.format(message, entries::get, 3, "default");
        System.out.println(" ");+
        System.out.println(str);
    }*/

   /* public static void main(String[] args) {
        int times = 1000000;
        HashSet<String> set = new HashSet<>();
        for(int i = 0; i < times; i++) {
            String id = StringUtil.getUniqueId(8, System.nanoTime());
            System.out.println(id);
            set.add(id);
        }

        System.out.println("SIZE: " + set.size() + "/" + times);
    }*/

   /*public static void main(String[] args){
       MultiCache<UUID, String, Object> cache = new MultiCache<>();
       UUID uuid = UUID.randomUUID();

       cache.put(uuid, "test", 14, ExpiringMap.ExpirationPolicy.CREATED, 5, TimeUnit.SECONDS, new Consumer<Object>() {
           @Override
           public void accept(Object o) {
               System.out.println("RIP!");
           }
       });
       cache.remove(uuid, "test");
   }*/

}
