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
    Crossroads insert(CrossroadsController.CossroadsBo cossroads);
    CrossroadsVo detail(Long cossroadsId);
    VoideFrameVo frame(Long cossroadsId);

    boolean deleteByIds(String[] crossroadId);
    boolean saveline(CrossroadsController.CrossroadsLineBo lines);
    List<CrossroadsStats> corssStatsTable(Long cossroadsId);
    boolean deleteStats(String[] crossroadStatsId);
    boolean insertStats(CrossroadsStats stats);
    boolean updateStats(CrossroadsStats stats);
    Map<String, Collection<String>> inoutlink(Long cossroadsId);
    boolean runVehicleCounts(Long cossroadsId);
    void analyzeVideo(Long cossroadsId, HttpServletResponse response);

    void statusFlowImage(Long cossroadsId, HttpServletResponse response);
//    CossroadsStats select(Long id);

}
