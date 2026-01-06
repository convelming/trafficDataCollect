package com.convelming.roadflow.model;

import com.convelming.roadflow.model.proxy.NewsAnnexProxy;
import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.ColumnIgnore;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Table("news_annex")
@EntityProxy
public class NewsAnnex implements ProxyEntityAvailable<NewsAnnex, NewsAnnexProxy> {


    @Column(primaryKey = true)
    private Long id;
    private Long newsId;
    private String name;
    private String path;
    @JsonIgnore
    private long byteSize;
    private String size;
    private Long type; // 0 图片。1附件列表

    @ColumnIgnore
    private String url;

}
