package com.kevin.start.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池配置
 *
 * @author huang jiahui
 * @date 2022/4/23 22:04
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor executorService() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 12, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(),new MyThreadFactory());
        return executor;
    }



}
