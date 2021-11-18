package com.keivn.start.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 时间类配置
 * @author huang jiahui
 * @date 2021/11/15 15:10
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime time = LocalDateTime.now();
        this.strictInsertFill(metaObject,"createTime", LocalDateTime.class, time);
        this.strictInsertFill(metaObject,"updateTime", LocalDateTime.class, time);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject,"updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
