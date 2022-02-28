package com.augurit.service.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 *
 * @author huang jiahui
 * @date 2021/11/11 11:58
 */
@Data
public class Base {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private String createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

    /**
     * 乐观锁
     */
    @Version
    @JSONField(serialize = false)
    private Integer lockVersion;
}
