package com.convelming.roadflow.model;

import com.convelming.roadflow.model.proxy.LinkProjectStatsProxy;
import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Table("link_project_stats")
@EntityProxy
public class LinkProjectStats implements ProxyEntityAvailable<LinkProjectStats, LinkProjectStatsProxy> {

    @Column(primaryKey = true)
    private Long id;

    private Long projectId;

    private String linkId;

    private Double saturation;

    private String service;

    private String style;

    @JsonIgnore
    private String ipAddr;

}
