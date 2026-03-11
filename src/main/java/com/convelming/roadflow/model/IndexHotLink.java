package com.convelming.roadflow.model;

import com.convelming.roadflow.model.proxy.IndexHotLinkProxy;
import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Table("index_hot_link")
@EntityProxy
public class IndexHotLink implements ProxyEntityAvailable<IndexHotLink, IndexHotLinkProxy> {

    @Column(primaryKey = true)
    private Long id;
    private String name;
    private String link;
    private Long clickCount;
    private Date updateTime;

}
