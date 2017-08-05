package de.superioz.moo;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisTest {

    @Test
    public void connectingToRedisWorks(){
        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");

        RedissonClient client = Redisson.create(config);
        RList<String> list = client.getList("myList");
        System.out.println("List Before: " + list);
        client.getList("myList").add("foo");
        System.out.println("List After: " + list);
    }

}
