package com.convelming.roadflow.controller;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.model.LinkProject;
import com.convelming.roadflow.service.LinkProjectService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 流量调查数据
 */
@RestController
@RequestMapping("/link/project")
public class LinkProjectController {

    @Resource
    private LinkProjectService service;

    @PostMapping("/insert")
    public Result insert(@ModelAttribute BO bo, HttpServletRequest request) {

        double[][] xyarr = null;
        double[][][] holes = null;

        try {
            if (bo.xyarr != null) {
                JSONArray ja = JSON.parseArray(bo.xyarr);
                xyarr = new double[ja.size()][];
                for (int i = 0; i < ja.size(); i++) {
                    JSONArray xy = ja.getJSONArray(0);
                    xyarr[i] = new double[]{xy.getDouble(0), xy.getDouble(1)};
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("框选范围不正确");
        }

        LinkProject linkProject = new LinkProject();
        linkProject.setName(bo.name);
        linkProject.setCreator(bo.creator);
        linkProject.setProjectTime(bo.projectTime);
        linkProject.setIpAddr(request.getRemoteAddr());

        return Result.failOrOk(service.insert(linkProject, bo.file, xyarr));
    }

    @PostMapping("/update")
    public Result update(@RequestBody LinkProject linkProject, HttpServletRequest request) {
        linkProject.setIpAddr(request.getRemoteAddr());
        return Result.failOrOk(service.update(linkProject));
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.failOrOk(service.delete(id));
    }

    @GetMapping("/list")
    public Result list(QueryParam param) {
        Page<LinkProject> page = new Page<>(param.pageNum, param.pageSize);
        page.param(
                new Object[]{"name", param.name},
                new Object[]{"creator", param.creator}
        );
        return Result.failOrOk(service.list(page));
    }

    /**
     * 框选新增示例值
     * @param sample
     * @return
     */
    @PostMapping("/add/sample")
    public Result sample(@RequestBody Sample sample) {
        return Result.ok(service.addSample(sample));
    }

    /**
     * 查询示例值
     * @param projectId
     * @param linkId
     * @return
     */
    @GetMapping("/query/sample")
    public Result querySample(Long projectId, String linkId) {
        return Result.ok(service.querySample(projectId, linkId));
    }

    // 演示值对象
    @Data
    public static class Sample {
        Long projectId;
        double[][] xyarr;
        double saturation;
        String service;
        String style;
    }

    @Data
    public static class BO {
        String name;
        String creator;
        MultipartFile file;
        @DateTimeFormat(pattern = "yyyy-MM")
        @JsonFormat(pattern = "yyyy-MM")
        Date projectTime;
        String xyarr;
        String holes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryParam {
        private Integer pageNum = 1;
        private Integer pageSize = 10;

        private String name;
        private String creator;

    }

}
