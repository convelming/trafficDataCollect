package com.convelming.roadflow.model;

import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.alibaba.fastjson.annotation.JSONField;
import com.convelming.roadflow.model.proxy.MapPictureProxy;
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
@ExcelTarget("linkStats")

@Table("map_picture")
@EntityProxy
public class MapPicture implements ProxyEntityAvailable<MapPicture, MapPictureProxy> {

    @Column(primaryKey = true)
    private long id;

    @JsonIgnore
    private PGgeometry geom;

    /**
     * x坐标
     */
    private double x;

    /**
     * y坐标
     */
    private double y;

    /**
     * 经度
     */
    private double lon;

    /**
     * 纬度
     */
    private double lat;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 文件名
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * ip地址
     */
    @JsonIgnore
    private String ipAddr;

    /**
     * 版本
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
     * 更新时间
     */
    @JsonIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 下载地址
     */
    @ColumnIgnore
    private String url;
}
