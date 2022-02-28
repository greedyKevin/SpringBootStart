package com.augurit.service.config;

import com.augurit.service.config.serializer.FastJson2JsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis 配置
 *
 * @author huang jiahui
 * @date 2021/11/19
 */
@Configuration
public class RedisConfig {

    /**
     * redis序列化配置
     *
     * @param factory
     * @return {@link RedisTemplate}<{@link String}, {@link Object}>
     */
    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
//      Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
//      ObjectMapper om = new ObjectMapper();
//      om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//      om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//      jackson2JsonRedisSerializer.setObjectMapper(om);

        FastJson2JsonRedisSerializer<Object> fastJson2JsonRedisSerializer = new FastJson2JsonRedisSerializer(Object.class);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用fastjson
        template.setValueSerializer(fastJson2JsonRedisSerializer);
        // hash的value序列化方式采用fastjson
        template.setHashValueSerializer(fastJson2JsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }
}
