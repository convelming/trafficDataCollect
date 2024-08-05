package com.convelming.roadflow.model;

import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.alibaba.fastjson.annotation.JSONField;
import com.convelming.roadflow.model.proxy.CossroadsProxy;
import com.easy.query.core.annotation.*;
import com.easy.query.core.basic.extension.logicdel.LogicDeleteStrategyEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelTarget("cossroads")

@Table("cossroads")
@EntityProxy
public class Cossroads implements ProxyEntityAvailable<Cossroads, CossroadsProxy> {

    @Column(primaryKey = true)
    private Long id;

    /**
     * 十字路范围中心点, json 字符串
     */
    private String center;

    /**
     * 十字路范围顶点, json 字符串
     */
    private String vertex;

    /**
     * 范围内link_id, json 字符串
     */
    private String inLinkId;

    /**
     * 视频
     */
    private String video;

    /**
     * 类型
     */
    private String type;

    /**
     * 绘制状态
     */
    private Integer status;

    /**
     * 绘制线数据
     */
    private String lines;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 登记ip地址
     */
    @JsonIgnore
    private String ipAddr;

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
