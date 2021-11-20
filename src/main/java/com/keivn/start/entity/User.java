package com.keivn.start.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keivn.start.entity.base.Base;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * todo
 *
 * @author huang jiahui
 * @date 2021/11/20 15:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("test_user")
public class User extends Base {

    private String name;
    private String account;
    private String password;
}
