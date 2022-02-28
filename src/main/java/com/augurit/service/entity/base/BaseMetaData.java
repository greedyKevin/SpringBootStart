package com.augurit.service.entity.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 基本元数据
 * @author huang jiahui
 * @date 2021/12/15 14:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseMetaData {

    /**
     * 标题
     */
    private String title;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建者id
     */
    private String creatorId;

    /**
     * 主题
     */
    private String subject;

    /**
     * 类型
     */
    private String type;

    /**
     * 来源
     */
    private String source;

    /**
     * 日期
     */
    private LocalDateTime date;

    /**
     * 与其关联的其他
     */
    private List<BaseMetaData> relation;


    private boolean deleted;

    public void addRelation(BaseMetaData metaData){
       if(relation == null){
           relation = new ArrayList<>();
       }
       relation.add(metaData);
    }

    private BaseMetaData(Builder builder){
        this.title = builder.title;
        this.creator = builder.creator;
        this.creatorId = builder.creatorId;
        this.subject = builder.subject;
        this.type = builder.type;
        this.source = builder.source;
        this.date = builder.date;
        this.deleted = builder.deleted;
    }

    public static Builder builder(){
        return new Builder();
    }


    public static final class Builder{
        private String title;
        private String creator;
        private String creatorId;
        private String subject;
        private String type;
        private String source;
        private LocalDateTime date;
        private boolean deleted;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder creator(String creator) {
            this.creator = creator;
            return this;
        }

        public Builder creatorId(String creatorId) {
            this.creatorId = creatorId;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public Builder isDeleted(){
            this.deleted = true;
            return this;
        }

        public BaseMetaData build(){
            return new BaseMetaData(this);
        }
    }
}
