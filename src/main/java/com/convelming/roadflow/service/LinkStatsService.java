package com.convelming.roadflow.service;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.LinkStats;

import java.util.Collection;
import java.util.List;

public interface LinkStatsService {

    boolean insert(LinkStats stats);

    boolean update(LinkStats stats);

    boolean delete(Long id);

    List<LinkStats> queryAllMaker();

    Page<LinkStats> queryByArea(double[][] xyarr, Boolean all, Page<LinkStats> page);

    Page<LinkStats> queryByLinkId(Long linkId, Page<LinkStats> page);

    List<LinkStats> queryByIds(Collection<Long> ids);

    LinkStats queryById(Long id);

}
