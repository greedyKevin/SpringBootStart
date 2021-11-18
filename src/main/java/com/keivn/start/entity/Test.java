package com.keivn.start.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keivn.start.entity.base.Base;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author huang jiahui
 * @date 2021/11/11 11:45
 */
@Data
@TableName("test")
@AllArgsConstructor
@NoArgsConstructor
public class Test extends Base {

    /**
     * 名字
     * @mock 张三
     */
    private String name;

    private Integer size;

    private String bucketName;

    private String type;
}
