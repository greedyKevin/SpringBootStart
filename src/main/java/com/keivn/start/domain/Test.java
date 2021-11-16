package com.keivn.start.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.keivn.start.domain.base.Base;
import lombok.Data;

import java.util.Date;

/**
 * todo
 *
 * @author huang jiahui
 * @date 2021/11/11 11:45
 */
@Data
public class Test extends Base {

    /**
     * 名字
     * @mock 张三
     */
    private String name;
}
