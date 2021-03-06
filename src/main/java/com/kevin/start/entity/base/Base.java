package com.kevin.start.entity.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

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
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 乐观锁
     */
    @Version
    private Integer lockVersion;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean isDeleted;
}
