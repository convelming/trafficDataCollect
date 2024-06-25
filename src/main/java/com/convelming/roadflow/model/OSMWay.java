package com.convelming.roadflow.model;

import com.convelming.roadflow.model.proxy.OSMWayProxy;
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

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Table("osm_way")
@EntityProxy
public class OSMWay implements ProxyEntityAvailable<OSMWay, OSMWayProxy> {


    @Column(primaryKey = true)
    private String id;

    /**
     * 路名
     */
    private String name;

    /**
     * 版本号
     */
    private Integer version;

    private Date timestamp;

    private Long uid;

    private String user;

    @ColumnIgnore
    private Long chageset;

    /**
     * 节点
     */
    private String nodes;

    @JsonIgnore
    private PGgeometry geom4326;

    @JsonIgnore
    private PGgeometry geom3857;

    /**
     * 单向路, yes or no
     */
    private Boolean oneway = false;

    /**
     * 公路类型
     */
    private String highway;

    /**
     * 其他信息
     */
    private String other;

}
