package com.convelming.roadflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.postgis.jdbc.PGgeometry;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSMWay {

    private Long id;

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

    private Long chageset;

    /**
     * 节点
     */
    private String nodes;

    private PGgeometry geom4326;

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
