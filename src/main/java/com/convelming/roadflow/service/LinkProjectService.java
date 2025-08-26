package com.convelming.roadflow.service;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.controller.LinkProjectController;
import com.convelming.roadflow.model.LinkProject;
import com.convelming.roadflow.model.vo.LinkStatsChartVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LinkProjectService {

    LinkProject detail(Long id);

    Page<LinkProject> list(Page<LinkProject> page);

    LinkProject insert(LinkProject linkProject, MultipartFile file, double[][] xyarr);

    LinkProject update(LinkProject linkProject, MultipartFile file, double[][] xyarr);

    boolean delete(Long id);

    /**
     * 新增示例值
     */
    boolean addSample(LinkProjectController.Sample sample);

    List<List<LinkStatsChartVo>> querySample(Long areaProjectId, Long[] projectIds);

}
