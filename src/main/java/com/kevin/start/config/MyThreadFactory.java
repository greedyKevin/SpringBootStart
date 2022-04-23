package com.kevin.start.config;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工厂
 *
 * @author huang jiahui
 * @date 2022/4/23 22:28
 */
public class MyThreadFactory implements ThreadFactory {
    private final static AtomicInteger NUM = new AtomicInteger(0);
    private final static String NAME = "Kevin-";

    @Override
    public Thread newThread(Runnable r){
        NUM.incrementAndGet();
        return new Thread(r,NAME + NUM.get());
    }
}
