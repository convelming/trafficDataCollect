package com.convelming.roadflow.model;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.alibaba.fastjson.annotation.JSONField;
import com.convelming.roadflow.model.proxy.LinkStatsProxy;
import com.convelming.roadflow.util.GeomUtil;
import com.easy.query.core.annotation.*;
import com.easy.query.core.basic.extension.logicdel.LogicDeleteStrategyEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.postgis.jdbc.PGgeometry;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelTarget("linkStats")

@Table("link_stats")
@EntityProxy
public class LinkStats implements ProxyEntityAvailable<LinkStats, LinkStatsProxy> {

    @Column(primaryKey = true)
    private Long id;

    @Excel(name = "路段ID", width = 30)
    private String linkId;

    /**
     * osm路段id
     */
    private String wayId;

    @Excel(name = "开始时间", width = 30, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    @Excel(name = "结束时间", width = 30, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Excel(name = "调查方式", width = 30, replace = {"人工调查_1", "视频识别_2", "高德爬取_3", "其他_0"})
    private String type;

    @Excel(name = "pcu/h", width = 30, isImportField = "wayId")
    private Double pcuH;

    @ColumnIgnore
    @Excel(name = "link空间信息")
    private String linkLineString;

    @ColumnIgnore
    @Excel(name = "way空间信息")
    private String wayLineString;

    /**
     * 小客车
     */
    @Excel(name = "小客车")
    private Double scar = 0.;

    /**
     * 小货车
     */
    @Excel(name = "小货车")
    private Double struck = 0.;

    /**
     * 中客车
     */
    @Excel(name = "中客车")
    private Double mcar = 0.;

    /**
     * 中货车
     */
    @Excel(name = "中货车")
    private Double mtruck = 0.;

    /**
     * 大客车
     */
    @Excel(name = "大客车")
    private Double lcar = 0.;

    /**
     * 大货车
     */
    @Excel(name = "大货车")
    private Double ltruck = 0.;

    private Double x;

    private Double y;

    /**
     * 视频地址
     */
    private String video;

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
     * 是否是双向
     */
    private Boolean isTwoWay = false;

    public LinkStats(Map<String, Object> map) {
        this.id = (Long) map.get("id");
        this.linkId = (String) map.get("linkId");
        this.wayId = (String) map.get("wayId");
        this.beginTime = (Date) map.get("beginTime");
        this.endTime = (Date) map.get("endTime");
        this.type = (String) map.get("type");
        this.pcuH = ((BigDecimal) map.get("pcuH")).doubleValue();
        this.linkLineString = map.get("linkLineString").toString();
        this.wayLineString = map.get("wayLineString").toString();
        this.scar = ((BigDecimal) map.get("scar")).doubleValue();
        this.struck = ((BigDecimal) map.get("struck")).doubleValue();
        this.mcar = ((BigDecimal) map.get("mcar")).doubleValue();
        this.mtruck = ((BigDecimal) map.get("mtruck")).doubleValue();
        this.lcar = ((BigDecimal) map.get("lcar")).doubleValue();
        this.ltruck = ((BigDecimal) map.get("ltruck")).doubleValue();
        this.x = ((BigDecimal) map.get("x")).doubleValue();
        this.y = ((BigDecimal) map.get("y")).doubleValue();
        this.video = (String) map.get("video");
        this.remark = (String) map.get("remark");
        this.ipAddr = (String) map.get("ipAddr");
        this.version = (Integer) map.get("version");
        this.deleted = (long) (Integer) map.get("deleted");
        this.createTime = (Date) map.get("createTime");
        this.updateTime = (Date) map.get("updateTime");
        this.isTwoWay = (Boolean) map.get("isTwoWay");
    }

}
