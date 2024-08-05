package com.convelming.roadflow.service;

import com.convelming.roadflow.controller.CossroadsController;
import com.convelming.roadflow.model.vo.VoideFrameVo;

import java.util.List;

public interface CossroadsService {

    boolean insert(CossroadsController.CossroadsBo cossroads, double[][] vertex);

    List<VoideFrameVo> frame(Long id);

    boolean saveline(List<CossroadsController.LineBo> lines);

}
