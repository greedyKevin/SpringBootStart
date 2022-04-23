package com.kevin.start;


import com.kevin.start.util.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import javax.annotation.Resource;


@SpringBootTest
class StartApplicationTests {

    @Autowired
    private RedisTemplate<String, Object> template;

    @Resource
    private RedisUtils redisUtils;


    @Test
    void contextLoads() {
        RedisConnection connection = template.getConnectionFactory().getConnection();
        System.out.println(connection.ping());
    }

}
