package com.convelming.roadflow.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.convelming.roadflow.model.proxy.LinkProjectProxy;
import com.easy.query.core.annotation.*;
import com.easy.query.core.basic.extension.logicdel.LogicDeleteStrategyEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Table("link_project")
@EntityProxy
public class LinkProject implements ProxyEntityAvailable<LinkProject, LinkProjectProxy> {

    @Column(primaryKey = true)
    private Long id;

    private String name;

    private String creator;

    @DateTimeFormat(pattern = "yyyy-MM")
    private Date projectTime;

    private String geomStr;

    private String geomFile;


    /**
     * ip地址
     */
    @JsonIgnore
    private String ipAddr;


    /**
     * 逻辑删除
     */
    @JsonIgnore
    @LogicDelete(strategy = LogicDeleteStrategyEnum.DELETE_LONG_TIMESTAMP)
    private Long deleted;

    /**
     * 创建时间
     */
    @JsonIgnore
    @UpdateIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
