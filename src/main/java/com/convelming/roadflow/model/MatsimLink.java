package com.convelming.roadflow.model;

import com.convelming.roadflow.model.proxy.MatsimLinkProxy;
import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.ColumnIgnore;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.postgis.jdbc.PGgeometry;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Table("matsim_link")
@EntityProxy
public class MatsimLink implements ProxyEntityAvailable<MatsimLink, MatsimLinkProxy> {

    /**
     * id
     */
    @Column(primaryKey = true)
    private String id;

    /**
     * 空间坐标编码
     */
    private Integer srid;

    /**
     * 起始点
     */
    private String fromNode;

    /**
     * 结束点
     */
    private String toNode;

    /**
     * 路名
     */
    private String name;

    /**
     * 长度
     */
    private Double length;

    /**
     * 自由速度
     */
    private Double freespeed;

    /**
     * 容量
     */
    private Double capacity;

    /**
     * 车道数
     */
    private Integer lane;

    /**
     * 空间坐标信息
     */
    @JsonIgnore
    private PGgeometry geom;

    /**
     * osm way id
     */
    private String origid;

    /**
     * 道路类型
     */
    private String type;

    /**
     * 中心点
     */
    @JsonIgnore
    private PGgeometry center;

    /**
     * 流量类型
     */
    @ColumnIgnore
    private String statsType;

    @ColumnIgnore
    private Double[] toxy;

    @ColumnIgnore
    private Double[] fromxy;

    public MatsimLink(MatsimLink link) {
        BeanUtils.copyProperties(link, this);
    }

}
