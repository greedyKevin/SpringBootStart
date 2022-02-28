package com.augurit.service.config;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 咖啡因缓存
 *
 * @author huang jiahui
 * @date 2022/1/11 11:24
 */
@Configuration
public class CaffeineCacheConfig {

    @Bean
    public Cache<String, Object> caffeineCache(){
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .initialCapacity(10)
                .maximumSize(50)
                .build();
    }
}
