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

    private Integer version;

    private Date timestamp;

    private Long uid;

    private String user;

    private Long chageset;

    private String nodes;

    private PGgeometry geom4326;

    private PGgeometry geom3857;

    /**
     * 单向路, yes or no
     */
    private Boolean oneway = false;

    private String highway;

    private String other;

}
