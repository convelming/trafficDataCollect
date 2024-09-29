package com.convelming.roadflow.service;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.controller.CrossroadsController;
import com.convelming.roadflow.model.Crossroads;
import com.convelming.roadflow.model.CrossroadsStats;
import com.convelming.roadflow.model.vo.CrossroadsVo;
import com.convelming.roadflow.model.vo.VoideFrameVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CrossroadsService {

    Page<Crossroads> list(Page<Crossroads> page);
    Crossroads insert(CrossroadsController.CossroadsBo crossroads);
    Crossroads updateById(Crossroads crossroads);
    CrossroadsVo detail(Long crossroadsId);
    VoideFrameVo frame(Long crossroadsId);

    boolean deleteByIds(String[] crossroadId);
    boolean saveline(CrossroadsController.CrossroadsLineBo lines);
    List<CrossroadsStats> corssStatsTable(Long cossroadsId);
    boolean deleteStats(String[] crossroadStatsId);
    boolean insertStats(CrossroadsStats stats);
    boolean updateStats(CrossroadsStats stats);
    Map<String, Collection<String>> inoutlink(Long crossroadsId);
    boolean runVehicleCounts(Long crossroadsId);
    void analyzeVideo(Long crossroadsId, HttpServletResponse response);

    void statusFlowImage(Long crossroadsId, HttpServletResponse response);
//    crossroadsStats select(Long id);

}
