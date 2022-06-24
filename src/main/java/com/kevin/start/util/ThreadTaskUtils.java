package com.kevin.start.util;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池工作工具类
 *
 * @author huang jiahui
 * @date 2022/6/24 9:29
 */
@AllArgsConstructor
@Component
public class ThreadTaskUtils {

    private final ThreadPoolExecutor executor;

    /**
     * 多线程批量增加
     *
     * @param list      列表
     * @param service   服务
     * @param chunkSize 批量大小
     */
    @Async
    public <M extends BaseMapper<T>, T> void saveBatch(List<T> list, ServiceImpl<M, T> service, int chunkSize) {
        int size = list.size();
        int num = size / chunkSize;

        if (list.size() % chunkSize != 0) {
            num++;
        }

        for (int i = 0; i < num; i++) {

            int finalI = i;

            if (finalI == num - 1) {
                executor.execute(() -> service
                        .saveBatch(list.subList(finalI * chunkSize, list.size()), chunkSize));

                return;
            }

            executor.execute(() -> service
                    .saveBatch(list.subList(finalI * chunkSize, (finalI + 1) * chunkSize), chunkSize));
        }
    }

}
