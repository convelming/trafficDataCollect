package com.convelming.roadflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.postgis.jdbc.PGgeometry;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSMNode {

    private Long id;

    private Integer version;

    private Date timestamp;

    private Long uid;

    private String user;

    private Long changeset;

    private Double lat;

    private Double lon;

    private Double x;

    private Double y;

    /**
     * 经纬度空间坐标
     */
    private PGgeometry geom4326;

    /**
     * 墨卡托空间坐标
     */
    private PGgeometry geom3857;

    private String other;

}
