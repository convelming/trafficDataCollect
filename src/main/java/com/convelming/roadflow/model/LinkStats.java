package com.convelming.roadflow.model;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelTarget("linkStats")
public class LinkStats {

    private Long id;

    @Excel(name = "路段ID", width = 30)
    private Long linkId;

    /**
     * osm路段id
     */
    private Long wayId;

    @Excel(name = "开始时间", width = 30, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    @Excel(name = "结束时间", width = 30, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Excel(name = "调查方式", width = 30, replace = {"人工调查_1", "视频识别_2", "其他_0"})
    private String type;

    @Excel(name = "pcu/h", width = 30, isImportField = "wayId")
    private Double pcuH;

    @Excel(name = "link空间信息")
    private String linkLineString;

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
    private Integer deleted;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonIgnore
    private Date updateTime;

    /**
     * 是否是双向
     */
    private Boolean isTwoWay;

}
