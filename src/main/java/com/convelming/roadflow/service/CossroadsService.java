package com.convelming.roadflow.service;

import com.convelming.roadflow.controller.CrossroadsController;
import com.convelming.roadflow.model.CrossroadsStats;
import com.convelming.roadflow.model.vo.VoideFrameVo;

import java.util.List;
import java.util.Map;

public interface CossroadsService {

    boolean insert(CrossroadsController.CossroadsBo cossroads, double[][] vertex);
    List<VoideFrameVo> frame(Long cossroadsId);
    boolean saveline(List<CrossroadsController.LineBo> lines);
    List<CrossroadsStats> corssStatsTable(Long cossroadsId);
    boolean deleteStats(Long crossroadStatsId);
    boolean insertStats(CrossroadsStats stats);
    boolean updateStats(CrossroadsStats stats);
    Map<String, List<String>> inoutlink(Long cossroadsId);


//    CossroadsStats select(Long id);

}
