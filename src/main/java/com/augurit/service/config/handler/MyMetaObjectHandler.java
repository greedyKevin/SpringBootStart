package com.augurit.service.config.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 数据库时间类配置
 * @author huang jiahui
 * @date 2021/11/15 15:10
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        String result = formatter.format(time);

        this.strictInsertFill(metaObject,"createTime", String.class, result);
        this.strictInsertFill(metaObject,"updateTime", String.class, result);
    }

    @Override
    public void updateFill(MetaObject metaObject) {

        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String result = formatter.format(time);
        this.strictUpdateFill(metaObject,"updateTime", String.class, result);
    }

    /**
     * 重写策略 有值也更新
     * @return {@link MetaObjectHandler}
     */
    @Override
    public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<?> fieldVal) {
        Object obj = fieldVal.get();
        if (Objects.nonNull(obj)){
            metaObject.setValue(fieldName,obj);
        }

        return this;
    }
}
