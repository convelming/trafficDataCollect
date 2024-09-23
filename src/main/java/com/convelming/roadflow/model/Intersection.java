package com.convelming.roadflow.model;


import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.alibaba.fastjson.annotation.JSONField;
import com.convelming.roadflow.model.proxy.IntersectionProxy;
import com.easy.query.core.annotation.*;
import com.easy.query.core.basic.extension.logicdel.LogicDeleteStrategyEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.postgis.jdbc.PGgeometry;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelTarget("intersection")

@Table("intersection")
@EntityProxy
public class Intersection implements ProxyEntityAvailable<Intersection, IntersectionProxy> {

    @Column(primaryKey = true)
    private Long id;

    /**
     * x坐标
     */
    private Double x;

    /**
     * y坐标
     */
    private Double y;

    /**
     * 名称
     */
    private String name;

    @JsonIgnore
    private PGgeometry geom;

    /**
     * 是否已有录入数据，0未录入，1已录入
     */
    private Integer status;

    /**
     * 版本号
     */
    @JsonIgnore
    private Integer version;

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

    /**
     * 修改时间
     */
    @JsonIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
