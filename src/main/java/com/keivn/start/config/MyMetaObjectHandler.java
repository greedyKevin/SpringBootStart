package com.keivn.start.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

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
        Date date = new Date();
        this.strictInsertFill(metaObject,"createTime", Date.class, date);
        this.strictInsertFill(metaObject,"modifyTime", Date.class, date);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject,"modifyTime", Date.class, new Date());
    }
}
