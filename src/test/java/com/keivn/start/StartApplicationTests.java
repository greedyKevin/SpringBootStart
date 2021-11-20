package com.keivn.start;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.keivn.start.utils.redis.RedisUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;

@SpringBootTest
class StartApplicationTests {

    @Autowired
    private RedisTemplate<String, Object> template;

    @Resource
    private RedisUtil redisUtil;


    @Test
    void contextLoads() {
//        redisUtil.hset("fileName","userA",0);
//        redisUtil.hset("fileName","userB",1);
//
//        System.out.println((Integer) redisUtil.hget("fileName", "userA") == 0);
//        redisUtil.hset("",test);
//
//
//        System.out.println(obj.getName());
//        template.opsForHash().put("123456","qwer",0);
//        template.opsForHash().put("123456","asdf",1);
//        template.opsForHash().put("123456","kkkg",2);
//        MinioClient minioClient = MinioClient.builder().endpoint("http://127.0.0.1:9000/")
//                .credentials("admin","12345678").build();
//
//
//        try {
//            minioClient.putObject(PutObjectArgs.builder().bucket("test").object("package123/")
//                    .stream(new ByteArrayInputStream(new byte[]{}),0,-1).build());
//        }catch (Exception e) {
//
//        }
    }

}
