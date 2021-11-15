package com.keivn.start.domain.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

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

    @TableField(fill = FieldFill.INSERT,update = "now()")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE,update = "now()")
    private Date modifyTime;

}
