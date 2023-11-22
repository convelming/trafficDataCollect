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
    private Integer pcuH;

    private Double x;

    private Double y;

    private String remark;

    @JsonIgnore
    private String ipAddr;

    @JsonIgnore
    private Integer version;

    @JsonIgnore
    private Integer deleted;

    @JsonIgnore
    private Date createTime;

    @JsonIgnore
    private Date updateTime;

}
