package com.convelming.roadflow.service;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.controller.LinkProjectController;
import com.convelming.roadflow.model.LinkProject;
import com.convelming.roadflow.model.LinkProjectStats;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LinkProjectService {


    Page<LinkProject> list(Page<LinkProject> page);

    LinkProject insert(LinkProject linkProject, MultipartFile file, double[][] xyarr);

    boolean update(LinkProject linkProject);

    boolean delete(Long id);

    /**
     * 新增示例值
     */
    boolean addSample(LinkProjectController.Sample sample);

    List<LinkProjectStats> querySample(Long[] projectIds, String[] linkIds);

}
