package com.augurit.service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 回收站实体类
 * @author huang jiahui
 * @date 2021/12/29 9:24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Trash {
    private String name;
    private String path;
    private String objectName;
    private List<Trash> objects;

    public void addChild(Trash trash){
        if(objects == null){
            objects = new ArrayList<>();
        }
        this.objects.add(trash);
    }
}
