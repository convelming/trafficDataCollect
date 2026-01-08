package com.convelming.roadflow.model;

import com.convelming.roadflow.model.proxy.NewsProxy;
import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.ColumnIgnore;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Table("news")
@EntityProxy
public class News implements ProxyEntityAvailable<News, NewsProxy> {


    @Column(primaryKey = true)
    private Long id;
    private String title;
    private String author;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;
    private String content;
    private String photo;
    private int type;
    @JsonIgnore
    private int sort;
    @JsonIgnore
    private int display;

    @ColumnIgnore
    private List<NewsAnnex> annexs; // 附件列表
}
