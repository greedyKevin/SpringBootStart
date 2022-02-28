package com.augurit.service.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.augurit.service.entity.base.Base;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类
 *
 * @author huang jiahui
 * @date 2021/11/20 15:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("oss_user")
public class User extends Base {

    /**
     * 姓名
     */
    private String name;
    /**
     * 账号
     */
    private String account;
    /**
     * 密码
     */
    @JSONField(serialize = false)
    private String password;

    /**
     * 逻辑删除
     */
    @TableLogic
    @JSONField(serialize = false)
    private Boolean isDeleted;
}
